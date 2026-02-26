<script setup lang="ts">
import { onMounted, reactive, ref, watch } from "vue";
import { deleteJson, getJson, postJson, putJson } from "../../utils/api";
import type { PageResponse } from "../../utils/page";
import { usePagination } from "../../utils/page";
import { useToast } from "../../utils/toast";
import Pagination from "../utils/Pagination.vue";

type PointChangeType =
  | "ACTIVITY_EARN"
  | "EXCHANGE_USE"
  | "ADMIN_ADJUST"
  | "SYSTEM_BONUS";

interface PointRecord {
  id: number;
  volunteerId: number;
  volunteerName: string;
  changeType: PointChangeType;
  changePoints: number;
  balanceAfter: number;
  reason: string;
  note: string;
  relatedRecordType: string;
  relatedRecordId: number;
  changeTime: string;
}

interface Volunteer {
  id: number;
  name: string;
  phone: string;
  status: string;
}

const { success, error } = useToast();

const pagination = usePagination(8);
const loading = ref(false);
const records = ref<PointRecord[]>([]);

const keyword = ref("");
const typeFilter = ref<string>("ALL");

const typeOptions = [
  { label: "全部", value: "ALL" },
  { label: "活动获得", value: "ACTIVITY_EARN" },
  { label: "兑换消耗", value: "EXCHANGE_USE" },
  { label: "管理员调整", value: "ADMIN_ADJUST" },
  { label: "系统奖励", value: "SYSTEM_BONUS" },
];

const typeLabelMap: Record<PointChangeType, string> = {
  ACTIVITY_EARN: "活动获得",
  EXCHANGE_USE: "兑换消耗",
  ADMIN_ADJUST: "管理员调整",
  SYSTEM_BONUS: "系统奖励",
};

const getChangeTypeLabel = (
  type: PointChangeType | string | undefined
): string => {
  if (!type) return "未知";
  return typeLabelMap[type as PointChangeType] || type;
};

const fetchPointChangeRecords = async (page: number) => {
  loading.value = true;
  try {
    const params = new URLSearchParams({
      page: String(page),
      size: String(pagination.pageObject.value.pageSize),
      type: typeFilter.value,
    });

    if (keyword.value.trim()) {
      params.append("keyword", keyword.value.trim());
    }

    const data = await getJson<PageResponse<PointRecord>>(
      `/api/admin/point-records?${params.toString()}`
    );
    records.value = data.content;
    pagination.updatePageState(data);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "获取积分记录失败";
    error("加载失败", msg);
  } finally {
    loading.value = false;
  }
};

watch(typeFilter, () => {
  fetchPointChangeRecords(0);
});

onMounted(() => fetchPointChangeRecords(0));

const handleSearch = () => {
  fetchPointChangeRecords(0);
};

const clearSearch = () => {
  keyword.value = "";
  fetchPointChangeRecords(0);
};

const showDetailDialog = ref(false);
const currentRecord = ref<PointRecord | null>(null);

const openDetailDialog = (record: PointRecord) => {
  currentRecord.value = record;
  showDetailDialog.value = true;
};

const closeDetailDialog = () => {
  currentRecord.value = null;
  showDetailDialog.value = false;
};

const showEditDialog = ref(false);
const isEditing = ref(false);
const editForm = reactive({
  changePoints: 0,
  reason: "",
  note: "",
});

const openEditDialog = (record: PointRecord) => {
  currentRecord.value = record;
  editForm.changePoints = record.changePoints;
  editForm.reason = record.reason;
  editForm.note = record.note || "";
  isEditing.value = false;
  showEditDialog.value = true;
};

const closeEditDialog = () => {
  currentRecord.value = null;
  editForm.changePoints = 0;
  editForm.reason = "";
  editForm.note = "";
  isEditing.value = false;
  showEditDialog.value = false;
};

const validateEditForm = (): string | null => {
  if (editForm.changePoints === 0) {
    return "变动数量不能为0";
  }
  if (!editForm.reason.trim()) {
    return "变动原因不能为空";
  }
  if (editForm.reason.trim().length > 200) {
    return "变动原因长度不能超过200个字符";
  }
  if (editForm.note.trim().length > 200) {
    return "备注长度不能超过200个字符";
  }
  return null;
};

const saveEdit = async () => {
  if (isEditing.value) return;

  const validationError = validateEditForm();
  if (validationError) {
    error("验证失败", validationError);
    return;
  }

  isEditing.value = true;
  try {
    await putJson(`/api/admin/point-records/${currentRecord.value?.id}`, {
      changePoints: editForm.changePoints,
      reason: editForm.reason.trim(),
      note: editForm.note.trim() || null,
    });
    success("保存成功", "积分记录已更新");
    closeEditDialog();
    await fetchPointChangeRecords(pagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "更新积分记录失败";
    error("操作失败", msg);
  } finally {
    isEditing.value = false;
  }
};

const showDeleteDialog = ref(false);
const isDeleting = ref(false);

const openDeleteDialog = (record: PointRecord) => {
  currentRecord.value = record;
  showDeleteDialog.value = true;
};

const closeDeleteDialog = () => {
  currentRecord.value = null;
  showDeleteDialog.value = false;
};

const deleteRecord = async () => {
  if (isDeleting.value) return;

  isDeleting.value = true;
  try {
    await deleteJson(
      `/api/admin/point-records/${currentRecord.value?.id}/revert`
    );
    success("撤销成功", "积分记录已撤销");
    closeDeleteDialog();
    await fetchPointChangeRecords(pagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "撤销积分记录失败";
    error("操作失败", msg);
  } finally {
    isDeleting.value = false;
  }
};

const showAddDialog = ref(false);
const isAdding = ref(false);
const volunteerSearchKeyword = ref("");
const volunteerSearchResults = ref<Volunteer[]>([]);
const selectedVolunteer = ref<Volunteer | null>(null);
const isSearchingVolunteer = ref(false);

const addForm = reactive({
  volunteerId: 0,
  changeType: "ADMIN_ADJUST" as PointChangeType,
  changePoints: 0,
  reason: "",
  note: "",
});

const openAddDialog = () => {
  selectedVolunteer.value = null;
  volunteerSearchKeyword.value = "";
  volunteerSearchResults.value = [];
  addForm.volunteerId = 0;
  addForm.changeType = "ADMIN_ADJUST";
  addForm.changePoints = 0;
  addForm.reason = "";
  addForm.note = "";
  showAddDialog.value = true;
};

const closeAddDialog = () => {
  selectedVolunteer.value = null;
  volunteerSearchKeyword.value = "";
  volunteerSearchResults.value = [];
  addForm.volunteerId = 0;
  addForm.changeType = "ADMIN_ADJUST";
  addForm.changePoints = 0;
  addForm.reason = "";
  addForm.note = "";
  showAddDialog.value = false;
};

const searchVolunteers = async () => {
  const keyword = volunteerSearchKeyword.value.trim();
  if (!keyword) {
    volunteerSearchResults.value = [];
    return;
  }

  isSearchingVolunteer.value = true;
  try {
    const data = await getJson<PageResponse<Volunteer>>(
      `/api/admin/volunteer/search?keyword=${encodeURIComponent(
        keyword
      )}&page=0&size=10`
    );
    volunteerSearchResults.value = data.content;
  } catch (err) {
    const msg = err instanceof Error ? err.message : "搜索志愿者失败";
    error("搜索失败", msg);
  } finally {
    isSearchingVolunteer.value = false;
  }
};

const selectVolunteer = (volunteer: Volunteer) => {
  selectedVolunteer.value = volunteer;
  addForm.volunteerId = volunteer.id;
  volunteerSearchKeyword.value = volunteer.name;
  volunteerSearchResults.value = [];
};

const validateAddForm = (): string | null => {
  if (!addForm.volunteerId) {
    return "请选择志愿者";
  }
  if (addForm.changePoints === 0) {
    return "变动数量不能为0";
  }
  if (Math.abs(addForm.changePoints) > 10000) {
    return "变动数量不能超过10000";
  }
  if (!addForm.reason.trim()) {
    return "变动原因不能为空";
  }
  if (addForm.reason.trim().length > 200) {
    return "变动原因长度不能超过200个字符";
  }
  if (addForm.note.trim().length > 200) {
    return "备注长度不能超过200个字符";
  }
  return null;
};

const addRecord = async () => {
  if (isAdding.value) return;

  const validationError = validateAddForm();
  if (validationError) {
    error("验证失败", validationError);
    return;
  }

  isAdding.value = true;
  try {
    await postJson("/api/admin/point-records", {
      volunteerId: addForm.volunteerId,
      changePoints: addForm.changePoints,
      changeType: addForm.changeType,
      reason: addForm.reason.trim(),
      note: addForm.note.trim() || null,
    });
    success("添加成功", "积分记录已添加");
    closeAddDialog();
    await fetchPointChangeRecords(pagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "添加积分记录失败";
    error("操作失败", msg);
  } finally {
    isAdding.value = false;
  }
};

const getRelatedRecordTypeText = (type: string) => {
  switch (type) {
    case "SIGNUP":
      return "活动报名";
    case "EXCHANGE":
      return "商品兑换";
    default:
      return type || "无";
  }
};
</script>

<template>
  <div class="admin-points">
    <header class="page-header">
      <h2>积分管理</h2>
    </header>

    <section class="search-wrap">
      <input
        v-model.trim="keyword"
        type="text"
        placeholder="搜索志愿者姓名"
        @keyup.enter="handleSearch"
      />
      <button
        v-if="keyword"
        type="button"
        class="clear-button"
        @click="clearSearch"
      >
        ×
      </button>
      <button type="button" class="search-button" @click="handleSearch">
        搜索
      </button>
    </section>

    <section class="filters-wrap">
      <label class="filter-item">
        <span>变动类型</span>
        <select v-model="typeFilter" @change="fetchPointChangeRecords(0)">
          <option
            v-for="option in typeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </label>
    </section>

    <section class="toolbar">
      <button type="button" class="add-button" @click="openAddDialog">
        + 添加记录
      </button>
    </section>

    <section class="table-area">
      <table class="record-table">
        <thead>
          <tr>
            <th>志愿者姓名</th>
            <th>变动类型</th>
            <th>变动数量</th>
            <th>变动时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="loading-row">
            <td colspan="5">正在加载...</td>
          </tr>
          <tr v-else-if="records.length === 0" class="empty-row">
            <td colspan="5">暂无积分记录</td>
          </tr>
          <tr v-else v-for="record in records" :key="record.id">
            <td>{{ record.volunteerName }}</td>
            <td>{{ getChangeTypeLabel(record.changeType) }}</td>
            <td :class="record.changePoints > 0 ? 'positive' : 'negative'">
              {{ record.changePoints > 0 ? "+" : "" }}{{ record.changePoints }}
            </td>
            <td>{{ record.changeTime }}</td>
            <td>
              <div class="actions">
                <button
                  type="button"
                  class="icon-button"
                  title="详情"
                  @click="openDetailDialog(record)"
                >
                  👁
                </button>
                <button
                  type="button"
                  class="icon-button"
                  title="编辑"
                  @click="openEditDialog(record)"
                >
                  ✎
                </button>
                <button
                  type="button"
                  class="icon-button"
                  title="撤销"
                  @click="openDeleteDialog(record)"
                >
                  ↩
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <Pagination
        :page-object="pagination.pageObject.value"
        :items="records"
        :loading="loading"
        :page-ranges="pagination.pageRanges.value"
        :go-to-page="(page: number) => pagination.goToPage(page, fetchPointChangeRecords)"
        :prev-page="() => pagination.prevPage(fetchPointChangeRecords)"
        :next-page="() => pagination.nextPage(fetchPointChangeRecords)"
      />
    </section>

    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>积分变动详情</h3>
        <p><strong>志愿者姓名：</strong>{{ currentRecord?.volunteerName }}</p>
        <p>
          <strong>变动类型：</strong
          >{{ getChangeTypeLabel(currentRecord?.changeType) }}
        </p>
        <p>
          <strong>变动数量：</strong
          >{{ (currentRecord?.changePoints || 0) > 0 ? "+" : ""
          }}{{ currentRecord?.changePoints || 0 }}
        </p>
        <p>
          <strong>变动后余额：</strong>{{ currentRecord?.balanceAfter || 0 }}
        </p>
        <p><strong>变动时间：</strong>{{ currentRecord?.changeTime }}</p>
        <p><strong>变动原因：</strong>{{ currentRecord?.reason || "暂无" }}</p>
        <p><strong>备注：</strong>{{ currentRecord?.note || "暂无" }}</p>
        <p>
          <strong>关联记录类型：</strong
          >{{
            getRelatedRecordTypeText(currentRecord?.relatedRecordType || "")
          }}
        </p>
        <p>
          <strong>关联记录ID：</strong
          >{{ currentRecord?.relatedRecordId || "无" }}
        </p>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeDetailDialog">
            关闭
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEditDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>编辑积分记录</h3>
        <div class="form-row">
          <label>变动数量 <span class="required">*</span></label>
          <input
            v-model.number="editForm.changePoints"
            type="number"
            placeholder="请输入变动数量"
          />
        </div>
        <div class="form-row">
          <label>变动原因 <span class="required">*</span></label>
          <textarea
            v-model="editForm.reason"
            placeholder="请输入变动原因"
            rows="3"
          ></textarea>
        </div>
        <div class="form-row">
          <label>备注</label>
          <textarea
            v-model="editForm.note"
            placeholder="请输入备注（可选）"
            rows="2"
          ></textarea>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button"
            :disabled="isEditing"
            @click="saveEdit"
          >
            保存
          </button>
          <button type="button" class="cancel-button" @click="closeEditDialog">
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDeleteDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>撤销积分记录</h3>
        <p class="dialog-tip">
          确定要撤销此积分记录吗？撤销后将创建一条相反的积分变动记录。
        </p>
        <p><strong>志愿者：</strong>{{ currentRecord?.volunteerName }}</p>
        <p>
          <strong>原变动数量：</strong
          >{{ (currentRecord?.changePoints || 0) > 0 ? "+" : ""
          }}{{ currentRecord?.changePoints || 0 }}
        </p>
        <p><strong>原变动原因：</strong>{{ currentRecord?.reason }}</p>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button danger"
            :disabled="isDeleting"
            @click="deleteRecord"
          >
            确认撤销
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeDeleteDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showAddDialog" class="dialog-bg">
      <div class="dialog-area large">
        <h3>添加积分记录</h3>
        <div class="form-row">
          <label>志愿者 <span class="required">*</span></label>
          <div class="volunteer-search">
            <input
              v-model="volunteerSearchKeyword"
              type="text"
              placeholder="搜索志愿者姓名"
              @keyup.enter="searchVolunteers"
            />
            <button
              type="button"
              class="search-volunteer-btn"
              @click="searchVolunteers"
              :disabled="isSearchingVolunteer"
            >
              {{ isSearchingVolunteer ? "搜索中..." : "搜索" }}
            </button>
          </div>
          <div v-if="selectedVolunteer" class="selected-volunteer">
            已选择：{{ selectedVolunteer.name }}（ID:
            {{ selectedVolunteer.id }}）
          </div>
          <div v-if="volunteerSearchResults.length > 0" class="search-results">
            <div
              v-for="volunteer in volunteerSearchResults"
              :key="volunteer.id"
              class="search-result-item"
              @click="selectVolunteer(volunteer)"
            >
              {{ volunteer.name }}（ID: {{ volunteer.id }}）
            </div>
          </div>
        </div>
        <div class="form-row">
          <label>变动类型 <span class="required">*</span></label>
          <select v-model="addForm.changeType">
            <option value="ADMIN_ADJUST">管理员调整</option>
            <option value="SYSTEM_BONUS">系统奖励</option>
          </select>
        </div>
        <div class="form-row">
          <label>变动数量 <span class="required">*</span></label>
          <input
            v-model.number="addForm.changePoints"
            type="number"
            placeholder="正数为增加，负数为扣减"
          />
        </div>
        <div class="form-row">
          <label>变动原因 <span class="required">*</span></label>
          <textarea
            v-model="addForm.reason"
            placeholder="请输入变动原因"
            rows="3"
          ></textarea>
        </div>
        <div class="form-row">
          <label>备注</label>
          <textarea
            v-model="addForm.note"
            placeholder="请输入备注（可选）"
            rows="2"
          ></textarea>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button"
            :disabled="isAdding"
            @click="addRecord"
          >
            添加
          </button>
          <button type="button" class="cancel-button" @click="closeAddDialog">
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-points {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-header {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.search-wrap,
.filters-wrap,
.toolbar,
.table-area {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto auto auto;
  gap: 10px;
}

.search-wrap input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
}

.clear-button {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 16px;
  cursor: pointer;
}

.clear-button:hover {
  background: #e5e7eb;
}

.search-button {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 20px;
  cursor: pointer;
}

.search-button:hover {
  background: #1d4ed8;
}

.filters-wrap {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.filter-item {
  display: grid;
  font-size: 13px;
  color: #374151;
  gap: 6px;
}

.filter-item select {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
}

.add-button {
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  cursor: pointer;
  font-weight: 500;
}

.add-button:hover {
  background: #059669;
}

.table-area {
  padding: 0;
  overflow: hidden;
}

.record-table {
  width: 100%;
  border-collapse: collapse;
}

.record-table thead {
  background: #f8fafc;
}

.record-table th {
  padding: 14px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  border-bottom: 1px solid #e5e7eb;
}

.record-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
}

.record-table tr:last-child td {
  border-bottom: none;
}

.loading-row,
.empty-row {
  text-align: center;
  color: #6b7280;
  padding: 40px 0;
}

.positive {
  color: #10b981;
  font-weight: 500;
}

.negative {
  color: #ef4444;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 8px;
}

.icon-button {
  background: white;
  color: #111827;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-button:hover {
  background: #f8fafc;
  border-color: #9ca3af;
  transform: translateY(-1px);
}

.dialog-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 2;
}

.dialog-area {
  display: flex;
  flex-direction: column;
  width: min(420px, 90vw);
  background: white;
  padding: 20px;
  gap: 12px;
  border-radius: 12px;
  max-height: 80vh;
  overflow-y: auto;
}

.dialog-area.large {
  width: min(500px, 90vw);
}

.dialog-area h3 {
  text-align: center;
  margin: 0;
}

.dialog-tip {
  color: #6b7280;
  font-size: 13px;
  margin: 0;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row label {
  font-size: 13px;
  color: #374151;
}

.required {
  color: #ef4444;
}

.form-row input,
.form-row select,
.form-row textarea {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
  font-size: 14px;
}

.form-row textarea {
  resize: vertical;
}

.volunteer-search {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.search-volunteer-btn {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 16px;
  cursor: pointer;
}

.search-volunteer-btn:hover {
  background: #1d4ed8;
}

.search-volunteer-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.selected-volunteer {
  background: #f0fdf4;
  color: #166534;
  padding: 10px;
  border-radius: 8px;
  font-size: 13px;
}

.search-results {
  max-height: 150px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.search-result-item {
  padding: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.search-result-item:hover {
  background: #f8fafc;
}

.dialog-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 10px;
}

.dialog-actions button {
  min-width: 80px;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.dialog-actions button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.dialog-actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.confirm-button {
  background: #2563eb;
  color: white;
  border: none;
}

.confirm-button:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.confirm-button.danger {
  background: #ef4444;
}

.confirm-button.danger:hover {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.cancel-button {
  background: white;
  color: #111827;
  border: 1px solid #d1d5db;
}

.cancel-button:hover {
  background: #f8fafc;
  border-color: #9ca3af;
}

.close-button {
  background: #2563eb;
  color: white;
  border: none;
}

.close-button:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}
</style>
