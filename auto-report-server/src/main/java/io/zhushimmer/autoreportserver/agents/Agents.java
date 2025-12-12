package io.zhushimmer.autoreportserver.agents;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.tool.Toolkit;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class Agents {
    private final OpenAIChatModel model;

    public Agents(OpenAIChatModel model) {
        this.model = model;
    }

    /**
     * 创建一个 Manager ReAct 智能体
     */
    public ReActAgent getManagerAgent(ArrayList<Object> tools) {
        Toolkit toolkit = createToolkit(tools);
        Memory memory = new InMemoryMemory();

        return ReActAgent.builder()
                .name("任务调度专家")
                .sysPrompt(getPrompt("Manager"))
                .model(model)                    // 用于推理的 LLM
                .toolkit(toolkit)                // 智能体可用的工具
                .memory(memory)                  // 对话历史
                .build();
    }

    /**
     * 创建一个 Database ReAct 智能体
     */
    public ReActAgent getDatabaseAgent(ArrayList<Object> tools, Map<String, Map<String, Object>> presetParams) {
        Toolkit toolkit = new Toolkit();
        for (Object tool : tools) {
            toolkit.registration()
                    .tool(tool)
                    .presetParameters(presetParams)
                    .apply();
        }

        Memory memory = new InMemoryMemory();

        return ReActAgent.builder()
                .name("数据库专家")
                .sysPrompt("你是一个数据库专家，精通各种数据库的SQL语句操作，你只负责从数据库查询对应数据。")
                .model(model)                    // 用于推理的 LLM
                .toolkit(toolkit)                // 智能体可用的工具
                .memory(memory)                  // 对话历史
                .build();
    }

    /**
     * 创建一个 Analysis ReAct 智能体
     */
    public ReActAgent getAnalysisAgent(ArrayList<Object> tools) {
        Toolkit toolkit = createToolkit(tools);
        Memory memory = new InMemoryMemory();

        return ReActAgent.builder()
                .name("数据分析专家")
                .sysPrompt(getPrompt("Analysis"))
                .model(model)                    // 用于推理的 LLM
                .toolkit(toolkit)                // 智能体可用的工具
                .memory(memory)                  // 对话历史
                .build();
    }

    /**
     * 创建一个 Report ReAct 智能体
     */
    public ReActAgent getReportAgent(ArrayList<Object> tools) {
        Toolkit toolkit = createToolkit(tools);
        Memory memory = new InMemoryMemory();

        return ReActAgent.builder()
                .name("报告专家")
                .sysPrompt(getPrompt("Report"))
                .model(model)                    // 用于推理的 LLM
                .toolkit(toolkit)                // 智能体可用的工具
                .memory(memory)                  // 对话历史
                .build();
    }

    /**
     * 根据工具列表创建工具集
     */
    private Toolkit createToolkit(ArrayList<Object> tools) {
        Toolkit toolkit = new Toolkit();
        for (Object tool : tools) {
            toolkit.registerTool(tool);
        }
        return toolkit;
    }

    private String getPrompt(String agentName) {
        String filePath = "";
        String content = "";
        switch (agentName) {
            case "Manager":
                filePath = "prompt/manager_agent.md";
            case  "Database":
                filePath = "prompt/database_agent.md";
            case "Analysis":
                filePath = "prompt/analysis_agent.md";
            case "Report":
                filePath = "prompt/report_agent.md";
        }
        try {
            InputStream inputStream = new ClassPathResource(filePath).getInputStream();
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
        }
        return content;
    }
}
