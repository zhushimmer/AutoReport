package io.zhushimmer.autoreportserver.agents.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

public class DatabaseAgentAsTool {
    private final ReActAgent agent;

    public DatabaseAgentAsTool(ReActAgent agent) {
        this.agent = agent;
    }

    @Tool(description = "数据库专家，根据数据需求从数据库查询数据。")
    public String dataQuery(@ToolParam(name = "data_details", description = "需要查询的数据，明确的数据需求。") String data_details) {
        Msg inputMsg = Msg.builder()
                .name("任务调度专家")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("从数据库查询以下数据：" + data_details)
                        .build())
                .build();
        Msg response = agent.call(inputMsg).block();
        return response.getTextContent();
    }
}
