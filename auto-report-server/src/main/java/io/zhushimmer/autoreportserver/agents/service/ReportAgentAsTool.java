package io.zhushimmer.autoreportserver.agents.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.text.MessageFormat;

public class ReportAgentAsTool {
    private final ReActAgent agent;

    public ReportAgentAsTool(ReActAgent agent) {
        this.agent = agent;
    }

    @Tool(description = "报告撰写专家，根据数据分析结果撰写分析报告。")
    public String dataAnalysis(
            @ToolParam(name = "result", description = "数据分析结果。") String result
            ) {
        Msg inputMsg = Msg.builder()
                .name("任务调度专家")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("根据以下数据分析结果撰写报告：" + result)
                        .build())
                .build();
        Msg response = agent.call(inputMsg).block();
        return response.getTextContent();
    }
}
