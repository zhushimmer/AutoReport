package io.zhushimmer.autoreportserver.agents.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.zhushimmer.autoreportserver.agents.service.ApiService;

import java.util.HashMap;
import java.util.Map;

public class DatabaseService {

    @Tool(description = "列出数据库所有表和介绍")
    public String listTables(
            @ToolParam(name = "engine_url", description = "Engine url") String engine_url
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("engine_url", engine_url);
        return new ApiService().sendJsonAndGetResponse("/list_tables", data);
    }

    @Tool(description = "获取数据库某个表的结构")
    public String getTableStructure(
            @ToolParam(name = "engine_url", description = "Engine url") String engine_url,
            @ToolParam(name = "table_name", description = "数据库表名") String table_name
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("engine_url", engine_url);
        data.put("table_name", table_name);
        return new ApiService().sendJsonAndGetResponse("/get_table_structure", data);
    }

    @Tool(description = "执行SQL查询操作并缓存数据")
    public String cacheData(
            @ToolParam(name = "sql", description = "SQL查询语句") String sql,
            @ToolParam(name = "engine_url", description = "Engine url") String engine_url
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("sql", sql);
        data.put("engine_url", engine_url);
        return new ApiService().sendJsonAndGetResponse("/cache_data", data);
    }


}
