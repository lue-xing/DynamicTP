/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.dashboard.dev.server.remote;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class NacosProxyClientTests {

    // @Test
    public void testNacosConfigs() {
        String url = "http://127.0.0.1:8848/nacos/v1/cs/configs";
        String result;

        try {
            HttpResponse response = HttpRequest.get(url)
                    .form("dataId", "")
                    .form("group", "")
                    .form("appName", "")
                    .form("config_tags", "")
                    .form("pageNo", "1")
                    .form("pageSize", "100")
                    .form("tenant", "")
                    .form("search", "blur")
                    .execute();

            result = response.body();

            System.out.println("响应状态码：" + response.getStatus());
            System.out.println("响应内容：\n" + result);
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "调用失败：" + e.getMessage();
        }
    }
}
