<script setup lang="tsx">
import { onMounted, ref, watch, type VNodeRef } from "vue";
import { SettingIcon } from "tdesign-icons-vue-next";
import {
  type SSEChunkData,
  type AIMessageContent,
  type TdChatMessageConfigItem,
  type ChatRequestParams,
  type ChatServiceConfig,
  type TdChatbotApi,
  type UserMessage,
  type AIMessage,
  type ChatMessagesData,
  type TextContent,
  type ChatBaseContent,
  type UserMessageContent,
} from "tdesign-web-components";
import { MessagePlugin } from "tdesign-vue-next";
import { Text, type NarrativeTextSpec } from "@antv/t8";
import { fetchEventSource } from '@microsoft/fetch-event-source';

export type T8Content = ChatBaseContent<"t8", Object>;
declare global {
  interface AIContentTypeOverrides {
    t8: T8Content;
  }
}

const t8Items = ref<(() => void)[]>([])
document.documentElement.setAttribute("theme-mode", "dark");

// 流式数据加载中
const isStreamLoad = ref(false);

const query = ref("");
const loading = ref(false);
const settingVisible = ref(false);

const elements = ref<Map<string, VNodeRef>>(new Map<string, VNodeRef>());
const addElement = (id: string, el: Element | any, data: string) => {
  const item = chatList.value.find(m => m.id === id);
  if (el && item) {
    if (item.content?.[0]?.type == "t8" && item.status == 'complete') {
      const text = new Text(el!);
      text.theme("dark");
      text.schema(JSON.parse(data));
      text.render();
    }
  }

}

// 消息
const chatList = ref<ChatMessagesData[]>([
  {
    id: "0",
    role: "assistant",
    datetime: new Date().toLocaleString(),
    comment: "",
    content: [
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
            title: "所有商品的总价格是多少",
            prompt: "所有商品的总价格是多少？",
          },
        ],
      },
    ],
  }
]);

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
          query.value = content.prompt;
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

const handleClose = () => {
  settingVisible.value = false;
  loading.value = false;
};

interface ApiConfig {
  url: string;
  key: string;
  model_name: string;
}

interface DatabaseConfig {
  type: string;
  host: "";
  port: number | null;
  database: string;
  username: string;
  password: string;
}

interface Config {
  api: ApiConfig;
  database: DatabaseConfig;
}

const config = ref<Config>({
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
        "Content-Type": "application/json;charset=UTF-8",
      },
      body: JSON.stringify(config.value),
    });

    if (!response.ok) {
      throw new Error("网络响应失败");
    }

    const result = await response.json();
    MessagePlugin.success({ content: "保存成功！" });
  } catch (error) {
    MessagePlugin.error({ content: "保存失败:" + error });
  }

  saveButtonLoading.value = false;
  settingVisible.value = false;
};

onMounted(async () => {
  try {
    const res = await fetch("http://localhost:8186/api/config");
    if (!res.ok) throw new Error("Network response was not ok");
    const data = await res.json();
    config.value = data;
  } catch (error) {
    console.error("Fetch config error:", error);
  }
});

const inputEnter = () => {
  if (isStreamLoad.value || !query.value) {
    return;
  }

  const userMsg: UserMessage = {
    id: crypto.randomUUID(),
    role: 'user',
    status: 'complete',
    datetime: new Date().toLocaleString(),
    content: [
      {
        type: 'text',
        data: query.value,
      },
    ],
  };
  chatList.value.push(userMsg);
  handleData(query.value);
  query.value = "";
  loading.value = false;
  isStreamLoad.value = false;
};

const handleData = async (query: string) => {
  loading.value = true;
  isStreamLoad.value = true;
  const uid = crypto.randomUUID();
  let msgData = "";
  let index = 0;
  let isT8Msg = false;

  const aiMsg: ChatMessagesData = {
    id: uid,
    role: "assistant",
    datetime: new Date().toLocaleString(),
    comment: "",
    status: "pending",
    content: [],
  }
  chatList.value.push(aiMsg)

  // 建立连接
  fetchEventSource('http://localhost:8186/api/sse/normal', {
    method: 'POST',
    headers: {
      'Accept': 'text/event-stream',
      "Content-Type": "application/json;charset=UTF-8",
    },
    body: JSON.stringify({
      "uid": uid,
      "prompt": query,
      "think": false,
      "search": false
    }),
    onmessage(event) {
      if (event.event == 'error') {
        const item = chatList.value.find(m => m.id === uid);
        if (item) {
          item.status = "error";
          item.content = [{
            type: "text",
            status: "error",
            data: event.data,
          }]
        }
        return
      }

      if (event.event == 'message') {
        // 处理接收到的消息
        const jsonData = JSON.parse(event.data);
        const msg: string = jsonData.msg;

        // if (index == 0 && msg.trimStart().startsWith("{")) {
        //   isT8Msg = true;
        //   const item = chatList.value.find(m => m.id === uid);
        //   if (item) {
        //     item.status = "streaming";
        //     item.content = [{
        //       type: "t8",
        //       status: "streaming",
        //       data: "",
        //     }]
        //   }
        // }

        // if (isT8Msg) {}
        // const item = chatList.value.find(m => m.id === uid);
        // if (item && item.content && item.content[0]) {
        //   item.content[0].data += msg;
        // }

        index += 1
        msgData += msg;
      }
    },
    onerror(error) {
      console.error('Error:', error);
      const item = chatList.value.find(m => m.id === uid);
      if (item) {
        item.status = "error";
        item.content = [{
          type: "text",
          status: "error",
          data: "Error: " + error,
        }]
      }
    },
    onclose() {
      console.log('Connection closed!');
      if (msgData.length == 0) {
        return
      }

      let type: "text" | "markdown" | "thinking" | "image" | "search" | "suggestion" | "t8";
      if (msgData.startsWith("{")) {
        type = "t8"
      } else {
        type = "markdown"
      }

      const item = chatList.value.find(m => m.id === uid);
      if (item) {
        item.status = "complete";
        item.content = [{
          type: type,
          status: "complete",
          data: msgData,
        }]
      }

    }
  });
}

// Utility: delay for a given ms
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// 清空消息
const clearConfirm = function () {
  for (const fn of t8Items.value) {
    fn();
  }
  chatList.value.length = 0;
};

const handleChatScroll = (e: any) => {
  console.log('handleChatScroll', e);
};

const handleOperation = (type: string, msg: AIMessage, index: number) => {
  // 对当前消息设置状态
  if (type === 'good' || type === 'bad') {
    msg.comment = msg.comment === type ? '' : type;
  }
};

const getActionbar = (msg: AIMessage) => {
  // const actionBar = ['good', 'bad', 'replay', 'copy'];
  const actionBar = ['good', 'bad', 'copy'];
  if (msg.content && msg.content[0]?.type == "t8") {
    actionBar.pop()
  }
  return actionBar
}



</script>

<template>
  <div class="home backdrop-blur-sm">
    <div class="home-header shadow-md flex items-center">
      <div class="home-main-left">
        <img class="logo-img" src="../assets/logo.svg" alt="AutoReport" />
        <span>AutoReport</span>
      </div>
      <div class="home-main-right">
        <t-button variant="dashed" shape="round" size="large" @click="settingVisible = !settingVisible">
          <template #icon>
            <SettingIcon />
          </template>
          设置
        </t-button>
      </div>
    </div>
    <div class="home-main">
      <div class="home-main-message">
        <t-space direction="vertical" style="width: 100%;">
          <t-chat-list :clear-history="chatList.length > 4 && !isStreamLoad" @clear="clearConfirm"
            @scroll="handleChatScroll">
            <template v-for="(msg, index) in chatList" :key="index">
              <t-chat-message v-bind="messageProps(msg)" :role="msg.role" :content="msg.content"
                :datetime="msg.datetime" animation="gradient" :status="msg.status">
                <template v-if="msg.content && msg.content[0]?.type == 't8'" #content>
                  <template v-for="(contentItem, contentIndex) in msg.content" :key="contentIndex">
                    <div :ref="(el) => addElement(msg.id, el, contentItem.data as string)"></div>
                  </template>
                </template>
                <template v-if="msg.content && msg.role == 'assistant'" #actionbar>
                  <t-chat-actionbar :comment="msg.comment" :content="msg.content[0]?.data"
                    :action-bar="getActionbar(msg)" @actions="(type: string) => handleOperation(type, msg, index)" />
                </template>
              </t-chat-message>
            </template>
          </t-chat-list>
        </t-space>
      </div>
    </div>
    <div class="home-footer">
      <t-chat-sender v-model="query" :loading="isStreamLoad" :stop-disabled="loading" :textarea-props="{
        placeholder: '请提供数据分析任务～ Enter 发送，Shift+Enter 换行',
      }" @send="inputEnter">
        <template #suffix="{ renderPresets }">
          <component :is="renderPresets([])" />
        </template>
      </t-chat-sender>
    </div>
    <t-drawer destroyOnClose v-model:visible="settingVisible" size="medium" :on-confirm="handleClose">
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
      <t-space direction="vertical" size="medium" style="width: 100%; margin-top: 40px">
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
          <t-input-number v-model="config.database.port" theme="normal" :max="65535" :min="1"></t-input-number>
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
        <t-button @click="saveConfig" :loading="saveButtonLoading">保存</t-button>
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
  width: 100%;
  // height: calc(100vh - 60px);
  flex: 1;
  overflow-y: auto;
  -ms-overflow-style: none;
  /* IE/Edge 旧版 */
  scrollbar-width: none;
  /* Firefox */
  display: flex;
  padding: 20px 0;
  align-items: center;
  flex-direction: column;
}

.main::-webkit-scrollbar {
  display: none;
}

.home-main-message {
  width: 50%;
  min-width: 500px;
}

.home-footer {
  box-sizing: border-box;
  width: 50%;
  min-width: 500px;
  margin: auto;
  padding-bottom: 30px;
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
