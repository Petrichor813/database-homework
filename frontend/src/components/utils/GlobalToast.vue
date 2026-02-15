<script setup lang="ts">
import { useToast } from "../utils/toast";

const { state } = useToast();
</script>

<template>
  <transition name="toast-slide">
    <div v-if="state.visible" class="toast" :class="`toast-${state.type}`">
      <div class="toast-icon">
        <span v-if="state.type === 'success'">✓</span>
        <span v-else-if="state.type === 'info'">i</span>
        <span v-else>×</span>
      </div>
      <div class="toast-content">
        <p class="toast-title">{{ state.message }}</p>
        <p v-if="state.detail" class="toast-detail">
          <span v-if="state.type === 'error'">失败原因：</span>
          {{ state.detail }}
        </p>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.toast {
  min-width: 320px;
  max-width: 480px;
  position: fixed;
  display: flex;
  align-items: flex-start;
  top: 20px;
  left: 50%;
  padding: 14px 18px;
  gap: 12px;
  border-radius: 12px;
  transform: translateX(-50%);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.18);
  z-index: 2000;
}

.toast-success {
  background: #ecfdf3;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.toast-info {
  background: #d6e8ff;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.toast-error {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.toast-icon {
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.8);
  font-weight: 700;
  border-radius: 50%;
}

.toast-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.toast-detail {
  font-size: 13px;
  color: inherit;
  opacity: 0.8;
}

.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.25s ease;
}

.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style>
