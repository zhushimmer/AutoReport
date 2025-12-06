package io.zhushimmer.autoreportserver.agents;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.tool.Toolkit;

import java.util.ArrayList;

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
                .sysPrompt("你是一个任务调度专家，能进行数据分析任务的调度和评估。你需要将数据分析任务问题拆解三个部分：数据获取、数据分析与报告撰写，并调用对应的专家完成对应的子任务，最终生成数据分析报告。要求：1. 先分析问题，如果不是数据分析任务则直接回复“不支持该任务”，是数据分析任务则将任务拆分并执行；2. 最终报告需要以MarkDown格式输出。")
                .model(model)                    // 用于推理的 LLM
                .toolkit(toolkit)                // 智能体可用的工具
                .memory(memory)                  // 对话历史
                .build();
    }

    /**
     * 创建一个 Database ReAct 智能体
     */
    public ReActAgent getDatabaseAgent(ArrayList<Object> tools) {
        Toolkit toolkit = createToolkit(tools);
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
    public ReActAgent getDatabaseAgentAsTool(ArrayList<Object> tools) {
        Toolkit toolkit = createToolkit(tools);
        Memory memory = new InMemoryMemory();

        return ReActAgent.builder()
                .name("数据分析专家")
                .sysPrompt("你是一名数据分析专家，精通各种数据分析任务，可以熟练的使用Python进行数据分析。")
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
}
