import { readonly, ref } from "vue";

export type ToastType = "success" | "info" | "error";

interface ToastState {
  visible: boolean;
  type: ToastType;
  message: string;
  detail: string;
}

const state = ref<ToastState>({
  visible: false,
  type: "success",
  message: "",
  detail: "",
});

let hideTimer: ReturnType<typeof setTimeout> | null = null;

const show = (type: ToastType, message: string, detail = "") => {
  if (hideTimer) {
    clearTimeout(hideTimer);
  }

  state.value = {
    visible: true,
    type,
    message,
    detail,
  };

  hideTimer = setTimeout(() => {
    state.value.visible = false;
  }, 3000);
};

export const useToast = () => {
  return {
    state: readonly(state),
    success: (message: string, detail = "") => show("success", message, detail),
    info: (message: string, detail = "") => show("info", message, detail),
    error: (message: string, detail = "") => show("error", message, detail),
  };
};
