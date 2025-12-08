<script setup lang="tsx">
import { ref, watch } from "vue";
import { SettingIcon } from "tdesign-icons-vue-next";
import {
  type SSEChunkData,
  type AIMessageContent,
  type TdChatMessageConfigItem,
  type ChatRequestParams,
  type ChatMessagesData,
  type ChatServiceConfig,
  type TdChatbotApi,
} from "tdesign-web-components";

document.documentElement.setAttribute("theme-mode", "dark");

const settingVisible = ref(false);

// 默认初始化消息
const mockData: ChatMessagesData[] = [
  {
    id: "0",
    role: "assistant",
    content: JSON.parse(
      JSON.stringify([
        {
          type: "text",
          status: "complete",
          data: "欢迎使用AutoReport智能助手，你可以这样问我：",
        },
        {
          type: "suggestion",
          status: "complete",
          data: [
            {
              title: "帮我分析最近三天的销售额变化",
              prompt: "帮我分析最近三天的销售额变化？",
            },
            {
              title: "帮我分析哪个产品销量最高",
              prompt: "帮我分析哪个产品销量最高？",
            },
          ],
        },
      ])
    ),
  },
];

const chatRef = ref<TdChatbotApi | null>(null);
const activeR1 = ref(false);
const activeSearch = ref(false);
const reqParamsRef = ref({ think: true, search: false });
// 消息属性配置
const messageProps = (msg: ChatMessagesData): TdChatMessageConfigItem => {
  const { role, content } = msg;
  const thinking = content?.find((item) => item.type === "thinking");
  if (role === "user") {
    return {
      variant: "base",
      placement: "right",
      avatar: "/src/assets/avatar.jpg",
    };
  }
  if (role === "assistant") {
    return {
      name: "AutoReport",
      placement: "left",
      avatar: "/src/assets/assistant.png",
      actions: ["replay", "copy", "good", "bad"],
      handleActions: {
        good: async ({ message, active }) => {
          console.log("点赞", message, active);
        },
        bad: async ({ message, active }) => {
          console.log("点踩", message, active);
        },
        replay: ({ message, active }) => {
          console.log("自定义重新回复", message, active);
          chatRef.value?.regenerate();
        },
        searchItem: ({ content, event }) => {
          event.preventDefault();
          console.log("点击搜索条目", content);
        },
        suggestion: ({ content }) => {
          console.log("点击建议问题", content);
          chatRef.value?.addPrompt(content.prompt);
        },
      },
      chatContentProps: {
        thinking: {
          maxHeight: 100,
          layout: "block",
          collapsed: thinking?.status === "complete",
        },
      },
    };
  }
  return {};
};

// 聊天服务配置
const chatServiceConfig = ref<ChatServiceConfig>({
  // endpoint: `https://1257786608-9i9j1kpa67.ap-guangzhou.tencentscf.com/sse/normal`,
  endpoint: `http://localhost:8186/api/sse/normal`,
  stream: true,
  retryInterval: 3,
  maxRetries: 3,
  onRequest: (params: ChatRequestParams) => {
    const { prompt } = params;
    return {
      headers: {
        "Content-Type": "application/json",
        "X-Requested-With": "XMLHttpRequest",
      },
      body: JSON.stringify({
        uid: "auto-report",
        prompt,
        ...reqParamsRef.value,
      }),
    };
  },
  onMessage: (
    chunk: SSEChunkData,
    message?: ChatMessagesData
  ): AIMessageContent => {
    const { type, ...rest } = chunk.data as any;
    switch (type) {
      case "think":
        return {
          type: "thinking",
          status: /耗时/.test(rest?.title) ? "complete" : "streaming",
          data: {
            title: rest.title || "深度思考中",
            text: rest.content || "", // 深度克隆
          },
        };
      case "text":
        return {
          type: "markdown",
          data: rest?.msg || "",
        };
      default:
        return { type: "text", data: "" };
    }
  },
  onComplete: (isAborted: boolean, params?: RequestInit, result?: any) => {
    // console.log("onComplete", isAborted, params);
    return null;
  },
  onAbort: async () => {},
  onError: (err: Error | Response) => {
    // console.error("Chatservice Error:", err);
  },
});

// 监听状态变化
watch(
  [activeR1, activeSearch],
  ([newR1, newSearch]) => {
    reqParamsRef.value = {
      think: newR1,
      search: newSearch,
    };
  },
  { immediate: true }
);

// 发送者属性
const senderProps = {
  placeholder: "请提供数据分析任务～ Enter 发送，Shift+Enter 换行",
};

const handleClose = () => {
  settingVisible.value = false;
};

const config = ref({
  api: {
    url: "",
    key: "",
    model_name: "",
  },
  database: {
    type: "",
    host: "",
    port: null,
    database: "",
    username: "",
    password: "",
  },
});
const saveButtonLoading = ref(false);
const saveConfig = async () => {
  saveButtonLoading.value = true;
  try {
    const response = await fetch("http://localhost:8186/api/config", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(config.value),
    });

    if (!response.ok) {
      throw new Error("网络响应失败");
    }

    const result = await response.json();
    console.log("提交成功:", result);
    saveButtonLoading.value = false;
  } catch (error) {
    alert("提交失败，请重试");
  }
};
</script>

<template>
  <div class="home backdrop-blur-sm">
    <div class="home-header shadow-md flex items-center">
      <div class="home-main-left">
        <img class="logo-img" src="../assets/logo.svg" alt="AutoReport" />
        <span>AutoReport</span>
      </div>
      <div class="home-main-right">
        <t-button
          variant="dashed"
          shape="round"
          size="large"
          @click="settingVisible = !settingVisible"
        >
          <template #icon><SettingIcon /></template>
          设置
        </t-button>
      </div>
    </div>
    <div class="home-main">
      <t-chatbot
        ref="chatRef"
        :default-messages="mockData"
        :message-props="messageProps"
        :sender-props="senderProps"
        :chat-service-config="chatServiceConfig"
      >
        <template #sender-footer-prefix></template>
      </t-chatbot>
    </div>
    <t-drawer
      destroyOnClose
      v-model:visible="settingVisible"
      size="medium"
      :on-confirm="handleClose"
    >
      <template #header>设置</template>
      <t-space direction="vertical" size="medium" style="width: 100%">
        <h1 class="text-lg text-white">模型API</h1>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Base url</span>
          <t-input v-model="config.api.url" />
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Key</span>
          <t-input v-model="config.api.key" />
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Model name</span>
          <t-input v-model="config.api.model_name" />
        </t-space>
      </t-space>
      <t-space
        direction="vertical"
        size="medium"
        style="width: 100%; margin-top: 40px"
      >
        <h1 class="text-lg text-white">数据库</h1>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Type</span>
          <t-select v-model="config.database.type">
            <t-option key="mysql" label="MySQL" value="mysql" />
            <t-option key="postgresql" label="PostgreSQL" value="postgresql" />
          </t-select>
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Host</span>
          <t-input v-model="config.database.host" />
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Port</span>
          <t-input-number
            v-model="config.database.port"
            theme="normal"
            :max="65535"
            :min="1"
          ></t-input-number>
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Database</span>
          <t-input v-model="config.database.database" />
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Username</span>
          <t-input v-model="config.database.username" />
        </t-space>
        <t-space direction="vertical" :size="0" style="width: 100%">
          <span>Password</span>
          <t-input type="password" v-model="config.database.password" />
        </t-space>
      </t-space>
      <template #footer>
        <t-button @click="saveConfig" :loading="saveButtonLoading"
          >保存</t-button
        >
        <t-button variant="outline" @click="settingVisible = false">
          取消
        </t-button>
      </template>
    </t-drawer>
  </div>
</template>

<style scope lang="less">
.home {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  color: #fff;
}

.home-header {
  width: 100%;
  height: 60px;
  display: flex;
  justify-content: space-between;
  flex-shrink: 0;
}

.home-main-left {
  font-size: 20px;
  font-weight: 500;
  margin-left: 12px;
  height: 100%;
  display: flex;
  align-items: center;
}

.home-main-right {
  margin-right: 12px;
}

.logo-img {
  height: 80%;
}

.home-main {
  box-sizing: border-box;
  width: 50%;
  height: calc(100vh - 60px);
  margin: auto;
  padding: 30px 0;
}

t-chatbot {
  .model-select {
    display: flex;
    align-items: center;

    .t-select {
      width: 112px;
      height: var(--td-comp-size-m);
      margin-right: var(--td-comp-margin-s);

      .t-input {
        border-radius: 32px;
        padding: 0 15px;
      }

      .t-input.t-is-focused {
        box-shadow: none;
      }
    }

    .check-box {
      width: 112px;
      height: var(--td-comp-size-m);
      border-radius: 32px;
      box-sizing: border-box;
      flex: 0 0 auto;

      .t-button__text {
        display: flex;
        align-items: center;
        justify-content: center;

        span {
          margin-left: var(--td-comp-margin-s);
        }
      }
    }

    .check-box.is-active {
      border: 1px solid var(--td-brand-color-focus);
      background: var(--td-brand-color-light);
      color: var(--td-text-color-brand);
    }
  }
}

.t-input-number {
  width: 100% !important;
}
</style>
