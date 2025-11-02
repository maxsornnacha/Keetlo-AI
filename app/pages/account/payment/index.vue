<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <main class="mx-auto max-w-[1400px] min-h-[75dvh] text-black px-4 lg:px-8 py-8">
    <!-- Heading -->
    <div>
      <h1 class="text-2xl font-bold mb-1 reveal will-change-transform transition duration-500 ease-out">
        Your Orders
      </h1>
      <p class="text-gray-400 mb-6 reveal will-change-transform transition duration-500 ease-out">
        View and manage your orders and subscriptions.
      </p>
    </div>

    <!-- Table -->
<!-- ===== New, compact Orders list ===== -->
<!-- Desktop table -->
<div class="hidden md:block reveal will-change-transform transition duration-500 ease-out">
  <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg border border-gray-200">
    <thead>
      <tr class="bg-gray-200">
        <th class="px-4 py-3 text-left text-xs font-medium uppercase text-gray-700">Receipt</th>
        <th class="px-4 py-3 text-left text-xs font-medium uppercase text-gray-700">Plan</th>
        <th class="px-4 py-3 text-left text-xs font-medium uppercase text-gray-700">Period</th>
        <th class="px-4 py-3 text-left text-xs font-medium uppercase text-gray-700">Total</th>
        <th class="px-4 py-3 text-left text-xs font-medium uppercase text-gray-700">Status</th>
        <th class="px-4 py-3 text-right text-xs font-medium uppercase text-gray-700">Action</th>
      </tr>
    </thead>

    <tbody class="divide-y divide-gray-200 text-sm">
      <tr
        v-for="order in orders"
        :key="order.receiptId"
        class="hover:bg-gray-100 transition"
      >
        <!-- Receipt -->
        <td class="px-4 py-3">
          <div class="flex items-center gap-2">
            <span class="font-mono truncate max-w-[160px]" :title="order.receiptId">#{{ order.receiptId }}</span>
          </div>
          <div class="text-xs text-black/60" :title="formatDate(order.createdAt)">
            Created: {{ formatDate(order.createdAt) }}
          </div>
        </td>

        <!-- Plan -->
        <td class="px-4 py-3">
          <div class="font-medium">{{ order.planName || '—' }}</div>
          <div class="text-xs text-black/60 line-clamp-1" :title="order.planDescription">{{ order.planDescription }}</div>
        </td>

        <!-- Period -->
        <td v-if="order.startDate && order.endDate" class="px-4 py-3">
          <div>{{ formatDate(order.startDate) || '—' }}</div>
          <div><span class="text-yellow-500">to</span> {{ formatDate(order.endDate) || '—' }}</div>
        </td>
        <td v-else class="px-4 py-3 text-black/50">
          None
        </td>

        <!-- Total -->
        <td class="px-4 py-3 font-semibold">
          {{ money(order.amount, order.currency) }}
        </td>

        <!-- Status -->
        <td class="px-4 py-3">
          <span
            class="inline-block rounded-full px-2 py-1 text-xs font-medium"
            :class="statusClass(order.status)"
          >
            {{ order.status }}
          </span>
        </td>

        <!-- Action -->
        <td class="px-4 py-3 text-right">
          <a
            v-if="order.status === 'PAID'"
            :href="downloadHref(order)"
            target="_blank"
            class="inline-flex items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-1.5 text-sm hover:bg-gray-200"
          >
            View receipt
          </a>
          <span v-else class="text-black/50">None</span>
        </td>
      </tr>

      <tr v-if="!loading && orders.length === 0">
        <td colspan="6" class="px-4 py-6 text-center text-black/70">No orders found.</td>
      </tr>
    </tbody>
  </table>
</div>

<!-- Mobile cards -->
<div class="md:hidden grid gap-3 reveal will-change-transform transition duration-500 ease-out">
  <article
    v-for="order in orders"
    :key="order.receiptId"
    class="rounded-xl border border-gray-200 bg-gray-100 p-4"
  >
    <header class="flex flex-wrap items-start justify-between gap-3">
      <div>
        <div class="text-xs text-black/60">Receipt</div>
        <div class="font-mono text-sm">#{{ order.receiptId }}</div>
      </div>
      <span
        class="shrink-0 inline-block rounded-full px-2 py-1 text-[11px] font-medium"
        :class="statusClass(order.status)"
      >
        {{ order.status }}
      </span>
    </header>

    <div class="mt-3">
      <div class="text-xs text-black/60">Plan</div>
      <div class="font-medium">{{ order.planName || '—' }}</div>
      <div v-if="order.planDescription" class="text-xs text-black/60 line-clamp-2">
        {{ order.planDescription }}
      </div>
    </div>

    <div class="mt-3 grid grid-cols-2 gap-4">
      <div>
        <div class="text-xs text-black/60">Start</div>
        <div class="text-sm">{{ formatDate(order.startDate) || '—' }}</div>
      </div>
      <div>
        <div class="text-xs text-black/60">End</div>
        <div class="text-sm">{{ formatDate(order.endDate) || '—' }}</div>
      </div>
    </div>

    <div class="mt-3 flex items-center justify-between">
      <div>
        <div class="text-xs text-black/60">Total</div>
        <div class="font-semibold">{{ money(order.amount, order.currency) }}</div>
      </div>
      <a
        v-if="order.status === 'PAID'"
        :href="downloadHref(order)"
        target="_blank"
        class="rounded-md border border-gray-200 bg-white px-3 py-1.5 text-sm hover:bg-gray-200"
      >
        View Receipt
      </a>
    </div>

    <footer class="mt-3 text-xs text-black/50">
      Created: {{ formatDate(order.createdAt) }}
    </footer>
  </article>

  <div v-if="!loading && orders.length === 0" class="text-center text-black/70 py-8">
    No orders found.
  </div>
</div>


    <!-- Pagination -->
    <div class="mt-8 flex items-center justify-between">
      <div class="text-sm text-black/70">
        Showing
        <span class="text-black">{{ startIndex + 1 }}</span>–<span class="text-black">{{ endIndex }}</span>
        of <span class="text-black">{{ displayTotal }}</span>
      </div>

      <div class="flex items-center gap-2">
        <button
          v-tooltip="'Previous page'"
          class="cursor-pointer rounded-md border border-gray-200 px-3 py-2 text-sm disabled:opacity-40 hover:bg-gray-200"
          :disabled="page === 1"
          @click="page = Math.max(1, page - 1)"
        >
          Prev
        </button>

        <template v-for="(p, i) in pagesToShow" :key="`${p}-${i}`">
          <button
            v-if="p !== '...'"
            class="rounded-md px-3 py-2 text-sm border transition"
            :class="page === p
              ? 'bg-indigo-500 text-white'
                : 'bg-white border-gray-200 hover:bg-gray-200 cursor-pointer'"
            :aria-current="page === p ? 'page' : undefined"
            @click="page = p as number"
          >
            {{ p }}
          </button>
          <span v-else class="px-2 text-sm text-black/60 select-none" aria-hidden="true">…</span>
        </template>

        <button
          v-tooltip="'Next page'"
          class="cursor-pointer rounded-md border border-gray-200 px-3 py-2 text-sm disabled:opacity-40 hover:bg-gray-200"
          :disabled="page === totalPages"
          @click="page = Math.min(totalPages, page + 1)"
        >
          Next
        </button>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";

type Order = {
  receiptId: string;
  startDate?: string;
  endDate?: string;
  createdAt?: string;
  amount: number | string;
  currency: string;
  status: string;
  planName?: string;
  planDescription?: string;
  receiptUrl?: string;
};

const { formatDate } = useFormatTime();
const config = useRuntimeConfig();
const route = useRoute();
const router = useRouter();

/* ---------------- URL–synced pagination state ---------------- */
const page = ref(1);
const pageSize = ref(2);

/* ---------------- Data ---------------- */
const orders = ref<Order[]>([]);
const total = ref(0);
const loading = ref(false);
const errorMsg = ref<string | null>(null);

/* ---------------- URL <-> State sync helpers ---------------- */
function serialize() {
  return {
    page: page.value > 1 ? String(page.value) : undefined,
    pageSize: pageSize.value !== 10 ? String(pageSize.value) : undefined,
  };
}
function hydrateFromUrl() {
  const p = Number.parseInt(String(route.query.page ?? ""), 10);
  const ps = Number.parseInt(String(route.query.pageSize ?? ""), 10);
  page.value = Number.isFinite(p) && p > 0 ? p : 1;
  pageSize.value = Number.isFinite(ps) && ps > 0 ? ps : 10;
}
let updatingUrl = false;
function shallowEqualQuery(a: Record<string, unknown>, b: Record<string, unknown>) {
  const ka = Object.keys(a), kb = Object.keys(b);
  if (ka.length !== kb.length) return false;
  for (const k of ka) if (a[k] !== b[k]) return false;
  return true;
}
let urlTimer: number | undefined;
function scheduleUrlUpdate(resetPage = false) {
  if (resetPage) page.value = 1;
  if (urlTimer) window.clearTimeout(urlTimer);
  urlTimer = window.setTimeout(() => {
    const next = serialize();
    const cur = route.query as Record<string, unknown>;
    if (!shallowEqualQuery(next, cur)) {
      updatingUrl = true;
      router.replace({ query: next }).finally(() => (updatingUrl = false));
    }
  }, 200);
}
watch(() => route.query, () => {
  if (updatingUrl) return;
  const before = serialize();
  hydrateFromUrl();
  const after = serialize();
  if (!shallowEqualQuery(before, after)) fetchOrders();
});

/* ---------------- Fetch ---------------- */
function buildParams() {
  const offset = (page.value - 1) * pageSize.value;
  return { limit: pageSize.value, offset };
}
async function fetchOrders() {
  loading.value = true;
  errorMsg.value = null;
  try {
    const { data } = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/orders`,
      { params: buildParams() }
    );

    // If your controller returns { items, total, limit, offset }
    orders.value = data.list ?? [];
    total.value = Number(data.total ?? 0);

  } catch (err: unknown) {
    errorMsg.value = axios.isAxiosError(err) ? (err.response?.data ?? err.message) : String(err);
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

/* ---------------- Derived values for UI ---------------- */
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));
const startIndex = computed(() => (page.value - 1) * pageSize.value);
const endIndex = computed(() => Math.min(total.value, startIndex.value + orders.value.length));
const displayTotal = computed(() => total.value);

/* Ellipses window */
const pagesToShow = computed<(number | "...")[]>(() => {
  const curr = page.value;
  const last = totalPages.value;
  const delta = 1;
  const pages: (number | "...")[] = [];

  if (last <= 7) {
    for (let i = 1; i <= last; i++) pages.push(i);
    return pages;
  }
  const left = Math.max(2, curr - delta);
  const right = Math.min(last - 1, curr + delta);

  pages.push(1);
  if (left > 2) pages.push("...");
  for (let i = left; i <= right; i++) pages.push(i);
  if (right < last - 1) pages.push("...");
  pages.push(last);
  return pages;
});

/* ---------------- Debounced refetch ---------------- */
let refetchTimer: number | undefined;
watch([page, pageSize], () => {
  scheduleUrlUpdate(false);
  if (refetchTimer) window.clearTimeout(refetchTimer);
  refetchTimer = window.setTimeout(fetchOrders, 250);
});

/* ---------------- Mount ---------------- */
onMounted(() => {
  hydrateFromUrl();
  fetchOrders();
});

/* ---------------- Helpers ---------------- */
function downloadHref(o: Order) {
  return `/account/billing/receipt/${encodeURIComponent(o.receiptId)}`;
}

function statusClass(status?: string) {
  const s = (status || '').toLowerCase();
  if (s === 'active' || s === 'paid') return 'bg-emerald-500 text-green-50';
  if (s === 'pending' || s === 'processing') return 'bg-amber-700 text-amber-50';
  return 'bg-gray-700 text-gray-100';
}

function money(amount: number | string, currency = "USD") {
  const num = typeof amount === "number" ? amount : Number(String(amount).replace(/,/g, "")) || 0;
  try {
    return new Intl.NumberFormat(undefined, { style: "currency", currency }).format(num);
  } catch {
    return `${currency} ${num.toFixed(2)}`;
  }
}
</script>
