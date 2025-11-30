package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = {"spring.profiles.active=local",
        "seata.enabled=false"
}
)
public class ElasticDocumentTest {
    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;
    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.100.128:9200")
        ));
    }

    @Test
    void testConnect() {
        System.out.println(client);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
    @Test
    void testCreate() throws IOException {
        //准备数据
        Item item = itemService.getById(317578L);
        //对象转换，属性拷贝
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);

        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }
    @Test
    void testUpdate() throws IOException {
        UpdateRequest request = new UpdateRequest("items", "317578");
        request.doc(
                "price",25600
        );
        client.update(request, RequestOptions.DEFAULT);
    }
    @Test
    void testGet() throws IOException {
        GetRequest request = new GetRequest("items", "317578");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println("itemDoc = " + itemDoc);
    }
    @Test
    void testDelete() throws IOException {
        DeleteRequest request = new DeleteRequest("items", "317578");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulk() throws IOException {
        int PageNo = 1, PageSize = 500;
        BulkRequest request = new BulkRequest();
        while (true){
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(PageNo, PageSize));
            List<Item> records = page.getRecords();
            if(records == null || records.isEmpty()){
                return;
            }
            for (Item item : records) {
                request.add(new IndexRequest("items").id(item.getId().toString()).source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item,ItemDoc.class)),XContentType.JSON));
            }
            client.bulk(request, RequestOptions.DEFAULT);
            PageNo++;
        }
    }
}
