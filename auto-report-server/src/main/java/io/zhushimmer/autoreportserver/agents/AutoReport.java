package io.zhushimmer.autoreportserver.agents;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.OpenAIChatModel;
import io.zhushimmer.autoreportserver.agents.service.AnalysisAgentAsTool;
import io.zhushimmer.autoreportserver.agents.service.DatabaseAgentAsTool;
import io.zhushimmer.autoreportserver.agents.service.ReportAgentAsTool;

import java.text.MessageFormat;
import java.util.ArrayList;

public class AutoReport {
    private final ReActAgent managerAgent;

    public AutoReport() {
        // 加载配置
        ConfigManager configManager = new ConfigManager("config.properties");

        // 创建模型
        OpenAIChatModel model = OpenAIChatModel.builder()
                .baseUrl(configManager.getProperty("api.url"))
                .apiKey(configManager.getProperty("api.key"))
                .modelName(configManager.getProperty("api.model_name"))
                .build();

        Agents agents = new Agents(model);

        // Database Agent转换为Tool
        ArrayList<Object> databaseTools = new ArrayList<>();
        DatabaseAgentAsTool databaseAgentAsTool = new DatabaseAgentAsTool(agents.getDatabaseAgent(databaseTools));

        // Analysis Agent转换为Tool
        ArrayList<Object> analysisTools = new ArrayList<>();
        AnalysisAgentAsTool analysisAgentAsTool = new AnalysisAgentAsTool(agents.getDatabaseAgent(analysisTools));

        // Report Agent转换为Tool
        ArrayList<Object> reportTools = new ArrayList<>();
        ReportAgentAsTool reportAgentAsTool = new ReportAgentAsTool(agents.getDatabaseAgent(reportTools));

        // 创建Manager Agent
        ArrayList<Object> managerTools = new ArrayList<>();
        managerTools.add(databaseAgentAsTool);
        managerTools.add(analysisAgentAsTool);
        managerTools.add(reportAgentAsTool);
        managerAgent = agents.getManagerAgent(managerTools);
    }

    /**
     * 执行数据分析任务
     */
    public String runTask(String taskContent) {
        Msg inputMsg = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text(MessageFormat.format("请执行以下数据分析任务：<task>{0}</task>", taskContent))
                        .build())
                .build();
        Msg response = managerAgent.call(inputMsg).block();
        return response.getTextContent();
    }
}
