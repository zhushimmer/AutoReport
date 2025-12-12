package io.zhushimmer.autoreportserver.agents.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.zhushimmer.autoreportserver.agents.service.ApiService;

import java.util.HashMap;
import java.util.Map;

public class AnalysisService {
    @Tool(description = "根据数据id删除缓存数据")
    public String deleteData(
            @ToolParam(name = "data_id", description = "数据id") String data_id
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("data_id", data_id);
        return new ApiService().sendJsonAndGetResponse("/delete_data", data);
    }

    @Tool(description = "执行python数据分析代码")
    public String analyze(
            @ToolParam(name = "data_id", description = "数据id") String data_id,
            @ToolParam(name = "code", description = "Python代码") String code
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("data_id", data_id);
        data.put("code", code);
        return new ApiService().sendJsonAndGetResponse("/analyze", data);
    }
}
