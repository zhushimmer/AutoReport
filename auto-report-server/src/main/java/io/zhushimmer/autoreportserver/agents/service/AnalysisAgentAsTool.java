package io.zhushimmer.autoreportserver.agents.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.text.MessageFormat;

public class AnalysisAgentAsTool {
    private final ReActAgent agent;

    public AnalysisAgentAsTool(ReActAgent agent) {
        this.agent = agent;
    }

    @Tool(description = "数据分析专家，执行数据分析操作，并获取分析结果。")
    public String dataAnalysis(
            @ToolParam(name = "task", description = "数据分析任务描述。") String task,
            @ToolParam(name = "data", description = "需要分析的数据，json格式。") String data
            ) {
        Msg inputMsg = Msg.builder()
                .name("任务调度专家")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text(MessageFormat.format("根据以下任务描述和数据执行数据分析，可以使用的Python第三方包：pandas、numpy。\n<task>{0}</task>\n<data>{1}</data>", task, data))
                        .build())
                .build();
        Msg response = agent.call(inputMsg).block();
        return response.getTextContent();
    }
}
