package io.zhushimmer.autoreportserver.agents;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.formatter.openai.OpenAIMultiAgentFormatter;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.OpenAIChatModel;
import io.zhushimmer.autoreportserver.agents.service.AnalysisAgentAsTool;
import io.zhushimmer.autoreportserver.agents.service.DatabaseAgentAsTool;
import io.zhushimmer.autoreportserver.agents.service.ReportAgentAsTool;
import io.zhushimmer.autoreportserver.agents.tools.AnalysisService;
import io.zhushimmer.autoreportserver.agents.tools.DatabaseService;
import reactor.core.publisher.Flux;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

public class AutoReport {
    private final ReActAgent managerAgent;

    public AutoReport() {
        // 加载配置
        ConfigManager configManager = new ConfigManager();

        // 创建模型
        OpenAIChatModel model = OpenAIChatModel.builder()
                .baseUrl(configManager.getProperty("api.url"))
                .apiKey(configManager.getProperty("api.key"))
                .modelName(configManager.getProperty("api.model_name"))
                .stream(true)
                .formatter(new OpenAIMultiAgentFormatter())
                .build();

        Agents agents = new Agents(model);

        // Database Agent转换为Tool
        ArrayList<Object> databaseTools = new ArrayList<>();
        String engineUrl = getEngineUrl();
        Map<String, Map<String, Object>> presetParams = Map.of(
                "listTables", Map.of(
                        "engine_url", engineUrl
                ),
                "getTableStructure", Map.of(
                        "engine_url", engineUrl
                ),"cacheData", Map.of(
                        "engine_url", engineUrl
                )
        );
        databaseTools.add(new DatabaseService());
        DatabaseAgentAsTool databaseAgentAsTool = new DatabaseAgentAsTool(agents.getDatabaseAgent(databaseTools, presetParams));

        // Analysis Agent转换为Tool
        ArrayList<Object> analysisTools = new ArrayList<>();
        analysisTools.add(new AnalysisService());
        AnalysisAgentAsTool analysisAgentAsTool = new AnalysisAgentAsTool(agents.getAnalysisAgent(analysisTools));

        // Report Agent转换为Tool
        ArrayList<Object> reportTools = new ArrayList<>();
        ReportAgentAsTool reportAgentAsTool = new ReportAgentAsTool(agents.getReportAgent(reportTools));

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
    public Flux<Event> runTask(String taskContent) {
        Msg inputMsg = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text(MessageFormat.format("请判断并执行以下数据分析任务：<task>{0}</task>", taskContent))
                        .build())
                .build();
        return managerAgent.stream(inputMsg);
    }

    public String getEngineUrl() {
        ConfigManager configManager = new ConfigManager();
        return String.format("%s://%s:%s@%s:%s/%s",
                configManager.getProperty("database.type"),
                configManager.getProperty("database.username"),
                configManager.getProperty("database.password"),
                configManager.getProperty("database.host"),
                configManager.getProperty("database.port"),
                configManager.getProperty("database.database")
        );
    }
}
