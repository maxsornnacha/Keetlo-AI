<template>
  <main class="relative min-h-screen grid place-items-center overflow-hidden text-black">
    <!-- Soft gradient background -->
    <div class="pointer-events-none absolute inset-0 -z-10" aria-hidden="true">
      <div class="absolute -top-40 -left-40 h-[46rem] w-[46rem] rounded-full blur-3xl opacity-30 bg-[conic-gradient(at_left,_theme(colors.indigo.500),_theme(colors.sky.400),_theme(colors.emerald.400),_theme(colors.indigo.500))]" />
      <div class="absolute -bottom-56 -right-32 h-[42rem] w-[42rem] rounded-full blur-3xl opacity-25 bg-[radial-gradient(60%_60%_at_50%_50%,_theme(colors.purple.500),_transparent)]" />
    </div>

    <!-- Card -->
    <section class="w-full max-w-xl rounded-2xl lg:border lg:border-white/10 lg:bg-white/5 lg:backdrop-blur-md lg:shadow-2xl px-6 py-8 sm:px-8">
      <!-- States -->
      <div v-if="loading" class="h-[30vh] grid place-items-center text-black/70">Loading order…</div>
      <div v-else-if="errorMsg" class="h-[30vh] grid place-items-center text-red-300">{{ errorMsg }}</div>

      <template v-else>
        <!-- Success badge + shimmer ring -->
        <div class="mx-auto mb-6 grid place-items-center">
          <div class="relative">
            <span class="absolute inset-0 rounded-full animate-ping-slow ring-2 ring-emerald-400/30" aria-hidden="true" />
            <div class="flex h-16 w-16 items-center justify-center rounded-full bg-emerald-500/15 text-emerald-400 ring-1 ring-emerald-400/30">
              <UIcon name="i-lucide-check-circle" class="size-8" />
            </div>
          </div>
        </div>

        <header class="text-center space-y-2">
          <h1 class="text-3xl font-bold tracking-tight">
            {{ orderDetail.status?.toUpperCase() === 'PAID' ? 'Payment Successful' : 'Order Created' }}
          </h1>
          <p class="text-black/70">
            {{ orderDetail.status?.toUpperCase() === 'PAID'
              ? "You're all set! Your purchase is confirmed—jump right in and start building."
              : "We’ve recorded your order. Complete payment to access your plan." }}
          </p>
        </header>

        <!-- Order summary -->
        <div class="mt-8 rounded-xl border border-white/10 bg-white/5 p-5 sm:p-6" aria-labelledby="order-summary">
          <h2 id="order-summary" class="text-lg font-semibold">Order summary</h2>

          <dl class="mt-4 space-y-4">
            <div class="flex items-center justify-between gap-4">
              <dt class="text-black/70">Order number</dt>
              <dd class="flex flex-wrap items-center gap-2">
                <code class="font-mono text-sm bg-white/10 rounded px-2 py-1">
                  #{{ orderDetail.receiptId || "—" }}
                </code>
                <button
                  v-if="orderDetail.receiptId"
                  class="inline-flex items-center gap-1 rounded px-2 py-1 text-xs text-black/90 bg-white/10 hover:bg-white/15 transition"
                  :aria-label="copied ? 'Copied' : 'Copy order number'"
                  @click="copyOrder()"
                >
                  <UIcon :name="copied ? 'i-lucide-check' : 'i-lucide-copy'" class="size-3.5" />
                  <span>{{ copied ? "Copied" : "Copy" }}</span>
                </button>
              </dd>
            </div>

            <div class="flex items-center justify-between gap-4">
              <dt class="text-black/70">Date</dt>
              <dd class="text-black">{{ formattedDate }}</dd>
            </div>

            <div class="flex items-center justify-between gap-4">
              <dt class="text-black/70">Plan</dt>
              <dd class="text-black">{{ orderDetail.planName }}</dd>
            </div>

            <div class="flex items-center justify-between gap-4 border-t border-dashed border-white/10 pt-4">
              <dt class="text-black font-semibold">Total</dt>
              <dd class="text-black font-semibold">{{ formattedTotal }}</dd>
            </div>
          </dl>
        </div>

        <!-- Actions -->
        <div class="mt-6 grid gap-3 sm:grid-cols-2">
          <a href="/account/projects" class="w-full">
            <button class="cursor-pointer w-full h-11 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-black font-semibold transition-colors">
              Go to my projects
            </button>
          </a>

          <a
            v-if="orderDetail.receiptId && orderDetail.status?.toUpperCase() === 'PAID'"
            :href="`/account/billing/receipt/${orderDetail.receiptId}`"
            class="w-full"
          >
            <button class="cursor-pointer w-full h-11 rounded-lg bg-white/10 hover:bg-white/15 text-black font-medium transition-colors">
              View receipt
            </button>
          </a>
          <button
            v-else
            class="w-full h-11 rounded-lg bg-white/10 text-black/60 cursor-not-allowed"
            disabled
          >
            Receipt unavailable
          </button>
        </div>

        <!-- Secondary links -->
        <p class="mt-6 text-center text-sm text-black/60">
          Need help?
          <a class="text-indigo-300 hover:text-indigo-200" href="/contact-us">Contact support</a>
        </p>
      </template>
    </section>

    <!-- confetti (lightweight) -->
    <div class="pointer-events-none absolute inset-0 overflow-hidden">
      <div v-for="n in 18" :key="n" class="confetti" :style="confettiStyle(n)" aria-hidden="true" />
    </div>
  </main>
</template>

<script setup lang="ts">
import axios from "axios";
import { computed, ref, reactive, onMounted } from "vue";
import { useRoute } from "vue-router";
import type { Order } from "~/types/Order";

const config = useRuntimeConfig();
const route = useRoute();

// Use the path param: /orders/:invoice
const invoiceParam = String(route.params.invoice ?? route.query.receipt_id ?? "");

// Data / state
const orderDetail = reactive<Order>({
  startDate: "",
  receiptId: "",
  name: "",
  email: "",
  amount: 0,
  currency: "",
  status: "",
  createdAt: "",
  planName: "",
  planDescription: "",
  endDate: "",
} as unknown as Order);
const loading = ref(true);
const errorMsg = ref<string | null>(null);

// Fetch order
async function fetchOrder(id: string) {
  loading.value = true;
  errorMsg.value = null;
  try {
    const { data } = await api.get(`${config.public.NUXT_PUBLIC_API_BASE}/orders/${encodeURIComponent(id)}`);
    orderDetail.startDate = data.startDate ?? "";
    orderDetail.receiptId = data.receiptId ?? id;
    orderDetail.name = data.name ?? "";
    orderDetail.email = data.email ?? "";
    orderDetail.amount = data.amount ?? 0;
    orderDetail.currency = (data.currency ?? "USD").toString().toUpperCase();
    orderDetail.status = data.status ?? "";
    orderDetail.createdAt = data.createdAt ?? "";
    orderDetail.planName = data.planName ?? "";
    orderDetail.planDescription = data.planDescription ?? "";
    orderDetail.endDate = data.endDate ?? "";
    // If you keep currencySymbol on server:
    (orderDetail as Order).currencySymbol = data.currencySymbol ?? "";
  } catch (e: unknown) {
    if (axios.isAxiosError(e)) {
      if (e.response?.status === 404) errorMsg.value = "Order not found.";
      else if (e.response?.status === 401) errorMsg.value = "Please sign in to view this order.";
      else errorMsg.value = e.response?.data?.message || e.message || "Failed to load order.";
    } else {
      errorMsg.value = "Failed to load order.";
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (!invoiceParam) {
    errorMsg.value = "Missing order id.";
    loading.value = false;
    return;
  }
  fetchOrder(invoiceParam);
});

// Formatters
const formattedTotal = computed(() => {
  const amt = Number(orderDetail.amount ?? 0);
  const ccy = (orderDetail.currency || "USD").toUpperCase();
  try {
    return new Intl.NumberFormat(undefined, { style: "currency", currency: ccy }).format(amt);
  } catch {
    const symbol = (orderDetail as Order).currencySymbol || ccy;
    return `${symbol} ${amt.toFixed(2)}`;
  }
});

const formattedDate = computed(() => {
  const src = orderDetail.createdAt || new Date().toISOString();
  const d = new Date(src);
  if (isNaN(d.getTime())) return src;
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(d);
});

// Copy order #
const copied = ref(false);
async function copyOrder() {
  if (!orderDetail.receiptId) return;
  try {
    await navigator.clipboard.writeText(`#${orderDetail.receiptId}`);
    copied.value = true;
    setTimeout(() => (copied.value = false), 1500);
  } catch {
    // no-op
  }
}

// Confetti styles
function confettiStyle(i: number) {
  const colors = ["#34d399", "#60a5fa", "#a78bfa", "#f472b6", "#f59e0b"];
  const left = Math.random() * 100;
  const delay = Math.random() * 0.8;
  const duration = 2.2 + Math.random() * 1.1;
  const size = 6 + Math.random() * 8;
  const rotate = Math.random() * 360;
  const color = colors[i % colors.length];
  return {
    left: `${left}%`,
    width: `${size}px`,
    height: `${size * 0.4}px`,
    background: color,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    transform: `rotate(${rotate}deg)`,
  } as Record<string, string>;
}
</script>

<style scoped>
@keyframes fall {
  0% { transform: translateY(-120%) rotate(0deg); opacity: 0; }
  10% { opacity: 1; }
  100% { transform: translateY(110vh) rotate(360deg); opacity: 0; }
}
.confetti {
  position: absolute;
  top: -10%;
  border-radius: 1px;
  opacity: 0;
  animation-name: fall;
  animation-timing-function: cubic-bezier(0.2, 0.6, 0.35, 0.9);
  animation-iteration-count: 1;
}
.animate-ping-slow {
  animation: ping-slow 2.2s cubic-bezier(0, 0, 0.2, 1) infinite;
}
@keyframes ping-slow {
  0% { transform: scale(0.9); opacity: 0.6; }
  70% { transform: scale(1.2); opacity: 0; }
  100% { transform: scale(1.2); opacity: 0; }
}
</style>
