<script setup lang="ts">
import { ref, onMounted } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import {
  HeatmapChart,
  SankeyChart,
  ScatterChart,
  LineChart,
  RadarChart,
} from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
  CalendarComponent,
  LegendComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { getJson } from "../utils/api";
import { useToast } from "../utils/toast";

use([
  CanvasRenderer,
  HeatmapChart,
  SankeyChart,
  ScatterChart,
  LineChart,
  RadarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
  CalendarComponent,
  LegendComponent,
]);

const toast = useToast();

interface DashboardKPIResponse {
  totalServiceHours: number;
  totalActivities: number;
  activeVolunteers: number;
  totalPointsIssued: number;
}

interface VolunteerActivityHeatmapResponse {
  months: string[];
  weekdays: string[];
  data: number[][];
}

interface SankeyNode {
  name: string;
}

interface SankeyLink {
  source: string;
  target: string;
  value: number;
}

interface PointFlowSankeyResponse {
  nodes: SankeyNode[];
  links: SankeyLink[];
}

interface ActivityParticipationBubble {
  activityTitle: string;
  participantCount: number;
  totalHours: number;
  totalPoints: number;
}

interface ActivityParticipationBubbleResponse {
  data: ActivityParticipationBubble[];
}

interface ActivityTypeTrendResponse {
  months: string[];
  activityTypes: string[];
  data: number[][];
}

interface VolunteerRetentionResponse {
  months: string[];
  retentionRates: number[];
}

interface VolunteerGrowthRadarResponse {
  volunteerName: string;
  volunteerPhone: string;
  totalActivities: number;
  totalHours: number;
  totalPoints: number;
  activityParticipation: number;
  serviceQuality: number;
  continuity: number;
  initiative: number;
}

const kpiData = ref<DashboardKPIResponse | null>(null);
const heatmapYear = ref<string>("全部");
const sankeyYear = ref<string>("全部");
const bubbleYear = ref<string>("全部");
const trendYear = ref<string>("全部");
const retentionYear = ref<string>("全部");
const heatmapData = ref<VolunteerActivityHeatmapResponse | null>(null);
const sankeyData = ref<PointFlowSankeyResponse | null>(null);
const bubbleData = ref<ActivityParticipationBubbleResponse | null>(null);
const trendData = ref<ActivityTypeTrendResponse | null>(null);
const retentionData = ref<VolunteerRetentionResponse | null>(null);
const radarData = ref<VolunteerGrowthRadarResponse | null>(null);

const heatmapOption = ref<any>(null);
const sankeyOption = ref<any>(null);
const bubbleOption = ref<any>(null);
const trendOption = ref<any>(null);
const retentionOption = ref<any>(null);
const radarOption = ref<any>(null);
const searchKeyword = ref("");
const selectedVolunteer = ref<any>(null);
const volunteerSearchResults = ref<any[]>([]);
const isSearchingVolunteer = ref(false);
const hasSearched = ref(false);

const fetchKPI = async () => {
  try {
    const data = await getJson<DashboardKPIResponse>("/api/statistics/kpi");
    kpiData.value = data;
  } catch (error) {
    toast.error("获取KPI数据失败");
  }
};

const fetchHeatmapData = async () => {
  try {
    const year =
      heatmapYear.value === "全部" ? null : parseInt(heatmapYear.value);
    const params = year !== null ? `?year=${year}` : "";
    const data = await getJson<VolunteerActivityHeatmapResponse>(
      `/api/statistics/volunteer-activity-heatmap${params}`
    );
    heatmapData.value = data;
    updateHeatmapChart();
  } catch (error) {
    toast.error("获取热力图数据失败");
  }
};

const fetchBubbleData = async () => {
  try {
    const year =
      bubbleYear.value === "全部" ? null : parseInt(bubbleYear.value);
    const params = year !== null ? `?year=${year}` : "";
    const data = await getJson<ActivityParticipationBubbleResponse>(
      `/api/statistics/activity-participation-bubble${params}`
    );
    bubbleData.value = data;
    updateBubbleChart();
  } catch (error) {
    toast.error("获取气泡图数据失败");
  }
};

const fetchTrendData = async () => {
  try {
    const year = trendYear.value === "全部" ? null : parseInt(trendYear.value);
    const params = year !== null ? `?year=${year}` : "";
    const data = await getJson<ActivityTypeTrendResponse>(
      `/api/statistics/activity-type-trend${params}`
    );
    trendData.value = data;
    updateTrendChart();
  } catch (error) {
    toast.error("获取趋势图数据失败");
  }
};

const fetchRetentionData = async () => {
  try {
    const year =
      retentionYear.value === "全部" ? null : parseInt(retentionYear.value);
    const params = year !== null ? `?year=${year}` : "";
    const data = await getJson<VolunteerRetentionResponse>(
      `/api/statistics/volunteer-retention${params}`
    );
    retentionData.value = data;
    updateRetentionChart();
  } catch (error) {
    toast.error("获取留存率数据失败");
  }
};

const fetchRadarData = async () => {
  if (!selectedVolunteer.value || !selectedVolunteer.value.id) {
    toast.error("请先选择志愿者");
    return;
  }

  try {
    const data = await getJson<VolunteerGrowthRadarResponse>(
      `/api/statistics/volunteer-growth-radar?volunteerId=${selectedVolunteer.value.id}`
    );
    radarData.value = data;
    updateRadarChart();
  } catch (error) {
    toast.error("获取雷达图数据失败");
  }
};

const fetchSankeyData = async () => {
  try {
    const year =
      sankeyYear.value === "全部" ? null : parseInt(sankeyYear.value);
    const params = year !== null ? `?year=${year}` : "";
    const data = await getJson<PointFlowSankeyResponse>(
      `/api/statistics/point-flow-sankey${params}`
    );
    sankeyData.value = data;
    updateSankeyChart();
  } catch (error) {
    toast.error("获取桑基图数据失败");
  }
};

const updateHeatmapChart = () => {
  if (!heatmapData.value) {
    return;
  }

  const months = heatmapData.value.months;
  const weekdays = heatmapData.value.weekdays;
  const data = heatmapData.value.data;

  const heatmapDataArray: any[] = [];
  for (let i = 0; i < data.length; i++) {
    const row = data[i];
    if (!row) {
      continue;
    }
    for (let j = 0; j < row.length; j++) {
      heatmapDataArray.push([j, i, row[j]]);
    }
  }

  heatmapOption.value = {
    tooltip: {
      position: "top",
      formatter: function (params: any) {
        return `${weekdays[params.value[0]]} ${
          months[params.value[1]]
        }<br/>活跃度: ${params.value[2]}`;
      },
    },
    grid: {
      height: "70%",
      top: "5%",
      bottom: "0%",
    },
    xAxis: {
      type: "category",
      data: weekdays,
      splitArea: {
        show: true,
      },
    },
    yAxis: {
      type: "category",
      data: months,
      splitArea: {
        show: true,
      },
    },
    visualMap: {
      min: 0,
      max: Math.max(...heatmapDataArray.map((item) => item[2]), 1),
      calculable: true,
      orient: "horizontal",
      left: "center",
      top: "80%",
    },
    series: [
      {
        name: "志愿者活跃度",
        type: "heatmap",
        data: heatmapDataArray,
        label: {
          show: false,
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
};

const updateSankeyChart = () => {
  if (!sankeyData.value) {
    return;
  }

  sankeyOption.value = {
    tooltip: {
      trigger: "item",
      triggerOn: "mousemove",
    },
    series: [
      {
        type: "sankey",
        layout: "none",
        emphasis: {
          focus: "adjacency",
        },
        data: sankeyData.value.nodes,
        links: sankeyData.value.links,
        top: "5%",
        bottom: "0%",
        lineStyle: {
          color: "gradient",
          curveness: 0.5,
        },
        label: {
          color: "#1f2937",
        },
      },
    ],
  };
};

const updateBubbleChart = () => {
  if (!bubbleData.value) {
    return;
  }

  const data = bubbleData.value.data;
  const bubbleDataArray: any[] = [];
  for (let i = 0; i < data.length; i++) {
    bubbleDataArray.push([
      data[i].participantCount,
      data[i].totalHours,
      data[i].totalPoints,
      data[i].activityTitle,
    ]);
  }

  bubbleOption.value = {
    tooltip: {
      trigger: "item",
      formatter: function (params: any) {
        return `活动: ${params.data[3]}<br/>参与人数: ${params.data[0]}<br/>总时长: ${params.data[1]}小时<br/>总积分: ${params.data[2]}`;
      },
    },
    grid: {
      left: "10%",
      right: "10%",
      top: "10%",
      bottom: "10%",
    },
    xAxis: {
      type: "value",
      name: "参与人数",
      nameLocation: "middle",
      nameGap: 30,
    },
    yAxis: {
      type: "value",
      name: "总时长(小时)",
      nameLocation: "middle",
      nameGap: 30,
    },
    series: [
      {
        name: "活动参与度",
        type: "scatter",
        data: bubbleDataArray,
        symbolSize: function (data: any) {
          return Math.sqrt(data[2]) * 3;
        },
        itemStyle: {
          color: "#3b82f6",
          opacity: 0.6,
        },
        emphasis: {
          itemStyle: {
            color: "#2563eb",
            borderColor: "#1d4ed8",
            borderWidth: 2,
          },
        },
      },
    ],
  };
};

const updateTrendChart = () => {
  if (!trendData.value) {
    return;
  }

  const months = trendData.value.months;
  const activityTypes = trendData.value.activityTypes;
  const data = trendData.value.data;

  const seriesData: any[] = [];
  for (let i = 0; i < activityTypes.length; i++) {
    seriesData.push({
      name: activityTypes[i],
      type: "line",
      stack: "total",
      areaStyle: {
        opacity: 0.3,
      },
      emphasis: {
        focus: "series",
      },
      data: data[i],
    });
  }

  trendOption.value = {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "cross",
        label: {
          backgroundColor: "#6a7985",
        },
      },
    },
    legend: {
      data: activityTypes,
      top: "85%",
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      top: "5%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: months,
    },
    yAxis: {
      type: "value",
    },
    series: seriesData,
  };
};

const updateRetentionChart = () => {
  if (!retentionData.value) {
    return;
  }

  const months = retentionData.value.months;
  const retentionRates = retentionData.value.retentionRates;

  retentionOption.value = {
    tooltip: {
      trigger: "axis",
      formatter: function (params: any) {
        const param = params[0];
        return `${param.name}<br/>留存率: ${param.value}%`;
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "10%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: months,
    },
    yAxis: {
      type: "value",
      name: "留存率(%)",
      min: 0,
      max: 100,
    },
    series: [
      {
        name: "志愿者留存率",
        type: "line",
        data: retentionRates,
        smooth: true,
        lineStyle: {
          color: "#22c55e",
          width: 3,
        },
        areaStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: "rgba(34, 197, 94, 0.3)" },
              { offset: 1, color: "rgba(34, 197, 94, 0.05)" },
            ],
          },
        },
        itemStyle: {
          color: "#22c55e",
        },
        emphasis: {
          itemStyle: {
            color: "#16a34a",
            borderColor: "#15803d",
            borderWidth: 2,
          },
        },
      },
    ],
  };
};

const updateRadarChart = () => {
  if (!radarData.value) {
    return;
  }

  const data = radarData.value;
  selectedVolunteer.value = {
    name: data.volunteerName,
    phone: data.volunteerPhone,
  };

  radarOption.value = {
    tooltip: {
      trigger: "item",
    },
    legend: {
      data: ["志愿者成长"],
      top: "5%",
    },
    radar: {
      indicator: [
        { name: "活动参与", max: 100 },
        { name: "服务质量", max: 100 },
        { name: "连续性", max: 100 },
        { name: "主动性", max: 100 },
      ],
      center: ["50%", "55%"],
      radius: "60%",
    },
    series: [
      {
        name: "志愿者成长",
        type: "radar",
        data: [
          {
            value: [
              data.activityParticipation,
              data.serviceQuality,
              data.continuity,
              data.initiative,
            ],
            name: "当前志愿者",
          },
        ],
        areaStyle: {
          color: "rgba(59, 130, 246, 0.3)",
        },
        lineStyle: {
          color: "#3b82f6",
          width: 2,
        },
        itemStyle: {
          color: "#3b82f6",
        },
        emphasis: {
          lineStyle: {
            color: "#2563eb",
            width: 3,
          },
          areaStyle: {
            color: "rgba(37, 99, 235, 0.5)",
          },
        },
      },
    ],
  };
};

const onHeatmapYearChange = () => {
  fetchHeatmapData();
};

const onSankeyYearChange = () => {
  fetchSankeyData();
};

const onBubbleYearChange = () => {
  fetchBubbleData();
};

const onTrendYearChange = () => {
  fetchTrendData();
};

const onRetentionYearChange = () => {
  fetchRetentionData();
};

const exportChartPNG = (chartName: string) => {
  console.log(`导出${chartName}为PNG`);
};

const exportChartCSV = (chartName: string) => {
  console.log(`导出${chartName}数据为CSV`);
};

const searchVolunteer = async () => {
  const keyword = searchKeyword.value.trim();
  if (!keyword) {
    volunteerSearchResults.value = [];
    hasSearched.value = false;
    selectedVolunteer.value = null;
    radarOption.value = null;
    return;
  }

  isSearchingVolunteer.value = true;
  hasSearched.value = true;
  try {
    const data = await getJson<any>(
      `/api/admin/volunteer/search?keyword=${encodeURIComponent(
        keyword
      )}&page=0&size=10`
    );
    volunteerSearchResults.value = data.content || [];
  } catch (error) {
    toast.error("搜索志愿者失败");
  } finally {
    isSearchingVolunteer.value = false;
  }
};

const selectVolunteer = (volunteer: any) => {
  selectedVolunteer.value = {
    id: volunteer.id,
    name: volunteer.name,
    phone: volunteer.phone,
  };
  searchKeyword.value = volunteer.name;
  volunteerSearchResults.value = [];
  hasSearched.value = false;
  fetchRadarData();
};

onMounted(() => {
  fetchKPI();
  fetchHeatmapData();
  fetchSankeyData();
  fetchBubbleData();
  fetchTrendData();
  fetchRetentionData();
});
</script>

<template>
  <div class="dashboard">
    <header class="page-header">
      <div class="header-title">
        <h2>数据看板</h2>
        <p>展示核心统计指标与图表</p>
      </div>
    </header>

    <section class="kpi-grid">
      <div class="kpi-card">
        <p>累计服务时长</p>
        <h3>
          {{ kpiData ? kpiData.totalServiceHours + " 小时" : "加载中..." }}
        </h3>
      </div>
      <div class="kpi-card">
        <p>累计活动场次</p>
        <h3>{{ kpiData ? kpiData.totalActivities + " 场" : "加载中..." }}</h3>
      </div>
      <div class="kpi-card">
        <p>活跃志愿者</p>
        <h3>{{ kpiData ? kpiData.activeVolunteers + " 人" : "加载中..." }}</h3>
      </div>
      <div class="kpi-card">
        <p>积分发放</p>
        <h3>{{ kpiData ? kpiData.totalPointsIssued + " 分" : "加载中..." }}</h3>
      </div>
    </section>

    <section class="chart-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>志愿者活跃度热力图</h3>
          <select
            v-model="heatmapYear"
            @change="onHeatmapYearChange"
            class="time-select"
          >
            <option value="全部">全部</option>
            <option value="2022">2022年</option>
            <option value="2023">2023年</option>
            <option value="2024">2024年</option>
            <option value="2025">2025年</option>
            <option value="2026">2026年</option>
          </select>
        </div>
        <v-chart
          v-if="heatmapOption"
          :option="heatmapOption"
          class="chart-container"
        />
        <div v-else class="chart-img">热力图加载中...</div>
        <div class="chart-actions">
          <button
            class="export-pic"
            @click="exportChartPNG('志愿者活跃度热力图')"
          >
            导出图片
          </button>
          <button
            class="export-data"
            @click="exportChartCSV('志愿者活跃度热力图')"
          >
            导出数据
          </button>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>积分流转桑基图</h3>
          <select
            v-model="sankeyYear"
            @change="onSankeyYearChange"
            class="time-select"
          >
            <option value="全部">全部</option>
            <option value="2022">2022年</option>
            <option value="2023">2023年</option>
            <option value="2024">2024年</option>
            <option value="2025">2025年</option>
            <option value="2026">2026年</option>
          </select>
        </div>
        <v-chart
          v-if="sankeyOption"
          :option="sankeyOption"
          class="chart-container"
        />
        <div v-else class="chart-img">桑基图加载中...</div>
        <div class="chart-actions">
          <button class="export-pic" @click="exportChartPNG('积分流转桑基图')">
            导出图片
          </button>
          <button class="export-data" @click="exportChartCSV('积分流转桑基图')">
            导出数据
          </button>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>活动参与度气泡图</h3>
          <select
            v-model="bubbleYear"
            @change="onBubbleYearChange"
            class="time-select"
          >
            <option value="全部">全部</option>
            <option value="2022">2022年</option>
            <option value="2023">2023年</option>
            <option value="2024">2024年</option>
            <option value="2025">2025年</option>
            <option value="2026">2026年</option>
          </select>
        </div>
        <v-chart
          v-if="bubbleOption"
          :option="bubbleOption"
          class="chart-container"
        />
        <div v-else class="chart-img">气泡图加载中...</div>
        <div class="chart-actions">
          <button
            class="export-pic"
            @click="exportChartPNG('活动参与度气泡图')"
          >
            导出图片
          </button>
          <button
            class="export-data"
            @click="exportChartCSV('活动参与度气泡图')"
          >
            导出数据
          </button>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>活动类型趋势堆叠面积图</h3>
          <select
            v-model="trendYear"
            @change="onTrendYearChange"
            class="time-select"
          >
            <option value="全部">全部</option>
            <option value="2022">2022年</option>
            <option value="2023">2023年</option>
            <option value="2024">2024年</option>
            <option value="2025">2025年</option>
            <option value="2026">2026年</option>
          </select>
        </div>
        <v-chart
          v-if="trendOption"
          :option="trendOption"
          class="chart-container"
        />
        <div v-else class="chart-img">堆叠面积图加载中...</div>
        <div class="chart-actions">
          <button
            class="export-pic"
            @click="exportChartPNG('活动类型趋势堆叠面积图')"
          >
            导出图片
          </button>
          <button
            class="export-data"
            @click="exportChartCSV('活动类型趋势堆叠面积图')"
          >
            导出数据
          </button>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>志愿者留存率曲线图</h3>
          <select
            v-model="retentionYear"
            @change="onRetentionYearChange"
            class="time-select"
          >
            <option value="全部">全部</option>
            <option value="2022">2022年</option>
            <option value="2023">2023年</option>
            <option value="2024">2024年</option>
            <option value="2025">2025年</option>
            <option value="2026">2026年</option>
          </select>
        </div>
        <v-chart
          v-if="retentionOption"
          :option="retentionOption"
          class="chart-container"
        />
        <div v-else class="chart-img">留存率曲线图加载中...</div>
        <div class="chart-actions">
          <button
            class="export-pic"
            @click="exportChartPNG('志愿者留存率曲线图')"
          >
            导出图片
          </button>
          <button
            class="export-data"
            @click="exportChartCSV('志愿者留存率曲线图')"
          >
            导出数据
          </button>
        </div>
      </div>

      <div class="chart-card">
        <h3>志愿者成长雷达图</h3>
        <div class="search-box">
          <div class="volunteer-search-wrapper">
            <div class="volunteer-search">
              <div class="volunteer-input-wrapper">
                <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="搜索志愿者姓名"
                  @keyup.enter="searchVolunteer"
                />
                <button
                  v-if="searchKeyword"
                  type="button"
                  class="clear-button"
                  @click="searchKeyword = ''; searchVolunteer()"
                >
                  ×
                </button>
              </div>
              <button
                type="button"
                class="search-volunteer-button"
                @click="searchVolunteer"
                :disabled="isSearchingVolunteer"
              >
                {{ isSearchingVolunteer ? "搜索中..." : "搜索" }}
              </button>
            </div>
            <div v-if="volunteerSearchResults.length > 0" class="search-results">
              <div
                v-for="volunteer in volunteerSearchResults"
                :key="volunteer.id"
                class="search-result-item"
                @click="selectVolunteer(volunteer)"
              >
                {{ volunteer.name }}（{{ volunteer.phone }}）
              </div>
            </div>
            <div
              v-if="hasSearched && volunteerSearchResults.length === 0"
              class="no-results"
            >
              没有搜索到相关志愿者
            </div>
          </div>
          <div v-if="selectedVolunteer" class="selected-volunteer">
            已选择：{{ selectedVolunteer.name }}（{{ selectedVolunteer.phone }}）
          </div>
        </div>
        <v-chart
          v-if="radarOption"
          :option="radarOption"
          class="chart-container"
        />
        <div v-else class="chart-img">雷达图加载中...</div>
        <div class="chart-actions">
          <button
            class="export-pic"
            @click="exportChartPNG('志愿者成长雷达图')"
          >
            导出图片
          </button>
          <button
            class="export-data"
            @click="exportChartCSV('志愿者成长雷达图')"
          >
            导出数据
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="css">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-title h2 {
  margin: 0 0 8px 0;
}

.header-title p {
  color: #6b7280;
  margin: 0;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.kpi-card p {
  color: #6b7280;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.kpi-card h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.chart-header h3 {
  margin: 0;
  color: #1f2937;
  font-size: 20px;
  font-weight: 600;
  flex-shrink: 0;
}

.time-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.time-select:hover {
  border: 1px solid #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.time-select:focus {
  outline: none;
  border: 2px solid #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.chart-card h3 {
  margin: 0;
  color: #1f2937;
  font-size: 20px;
  font-weight: 600;
  flex-shrink: 0;
}

.chart-container {
  flex: 1;
  min-height: 300px;
}

.chart-img {
  flex: 1;
  min-height: 300px;
  background: #f1f5f9;
  border: 1px dashed #cbd5f5;
  border-radius: 8px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.chart-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.chart-actions button {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.export-pic {
  background: #22c55e;
  color: white;
}

.export-pic:hover {
  background: #16a34a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.export-data {
  background: #9ca3af;
  color: white;
}

.export-data:hover {
  background: #6b7280;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

.search-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.volunteer-search-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
}

.volunteer-search {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.volunteer-input-wrapper {
  position: relative;
}

.volunteer-input-wrapper input {
  width: 100%;
  padding-right: 40px;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
  transition: all 0.2s ease;
}

.volunteer-input-wrapper input:hover,
.volunteer-input-wrapper input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.volunteer-input-wrapper input:focus {
  border-width: 2px;
}

.clear-button {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  width: 28px;
  height: 28px;
  line-height: 1;
  background: #e5e7eb;
  color: #6b7280;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  padding: 0;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  transition: background 0.2s ease, color 0.2s ease;
}

.clear-button:hover {
  background: #d1d5db;
  color: #374151;
}

.search-volunteer-button {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-volunteer-button:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.search-volunteer-button:disabled {
  opacity: 0.45;
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
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 150px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-result-item {
  padding: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.search-result-item:hover {
  background: #f8fafc;
}

.no-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  padding: 10px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #ef4444;
  font-size: 13px;
  text-align: center;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.volunteer-info {
  padding: 8px 12px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  color: #1e40af;
  font-size: 14px;
}
</style>
