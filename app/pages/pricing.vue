<template>
  <main class="relative flex flex-1 flex-col items-center text-slate-100 overflow-x-hidden">
    <!-- Ambient background -->
    <div class="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute inset-0 bg-[#0b0f14]"/>
      <div
        class="absolute left-0 w-70 inset-0 opacity-70 rounded-full"
        :style="{
          backgroundImage: [
            `radial-gradient(1200px 700px at 50% -10%, rgba(99,102,241,0.45), rgba(99,102,241,0.08) 55%, rgba(99,102,241,0) 80%)`,
            `radial-gradient(1100px 800px at 50% 95%, rgba(255,172,70,0.45), rgba(255,120,110,0.2) 45%, rgba(255,120,110,0) 80%)`
          ].join(', ')
        }"
      />
      <div
        class="absolute left-80 w-70 inset-0 opacity-70 rounded-full"
        :style="{
          backgroundImage: [
            `radial-gradient(1200px 700px at 50% -10%, rgba(99,102,241,0.45), rgba(99,102,241,0.08) 55%, rgba(99,102,241,0) 80%)`,
            `radial-gradient(1100px 800px at 50% 95%, rgba(255,172,70,0.45), rgba(255,120,110,0.2) 45%, rgba(255,120,110,0) 80%)`
          ].join(', ')
        }"
      />
          <div
        class="absolute left-160 w-70 inset-0 opacity-70 rounded-full"
        :style="{
          backgroundImage: [
            `radial-gradient(1200px 700px at 50% -10%, rgba(99,102,241,0.45), rgba(99,102,241,0.08) 55%, rgba(99,102,241,0) 80%)`,
            `radial-gradient(1100px 800px at 50% 95%, rgba(255,172,70,0.45), rgba(255,120,110,0.2) 45%, rgba(255,120,110,0) 80%)`
          ].join(', ')
        }"
      />
           <div
        class="absolute left-240 w-70 inset-0 opacity-70 rounded-full"
        :style="{
          backgroundImage: [
            `radial-gradient(1200px 700px at 50% -10%, rgba(99,102,241,0.45), rgba(99,102,241,0.08) 55%, rgba(99,102,241,0) 80%)`,
            `radial-gradient(1100px 800px at 50% 95%, rgba(255,172,70,0.45), rgba(255,120,110,0.2) 45%, rgba(255,120,110,0) 80%)`
          ].join(', ')
        }"
      />
      <div
        class="absolute inset-0 opacity-50 mix-blend-overlay"
        :style="{ backgroundImage: 'radial-gradient(rgba(255,255,255,0.06) 1px, transparent 1.3px)', backgroundSize: '6px 6px' }"
      />
    </div>

    <div class="w-full max-w-7xl px-4 py-12 sm:py-16 lg:py-20">
      <!-- Heading -->
      <header class="mx-auto flex max-w-3xl flex-col items-center gap-4 text-center">
        <h1 class="bg-gradient-to-b from-white to-slate-300 bg-clip-text text-4xl font-extrabold tracking-tight text-transparent sm:text-5xl lg:text-6xl">
          Choose the plan that's right for you
        </h1>
        <p class="text-lg text-slate-400">
          Simple, transparent pricing. No hidden fees. Cancel anytime.
        </p>
      </header>

      <!-- Plans -->
      <section
        v-if="!loading"
        class="mx-auto mt-10 grid w-full grid-cols-1 gap-6 sm:gap-8"
        :class="subscriptionPlans.length > 1 ? 'md:grid-cols-2' : 'md:grid-cols-1'"
      >
        <div
          v-for="plan in subscriptionPlans"
          :key="plan.subscriptionPlanId"
          class="group relative min-h-[500px] overflow-hidden rounded-2xl border p-8 shadow-2xl transition
                 backdrop-blur-md"
          :class="plan.mostPopular === 0
            ? 'border-white/10 bg-white/5 ring-1 ring-white/10'
            : 'border-indigo-400/40 bg-gradient-to-b from-indigo-500/10 to-indigo-400/5 ring-1 ring-indigo-400/40'"
        >
          <!-- Popular badge -->
          <div
            v-if="plan.mostPopular === 1"
            class="absolute right-4 top-4 inline-flex items-center gap-2 rounded-full bg-indigo-500 px-3 py-1 text-[11px] font-semibold uppercase tracking-wide text-white shadow"
          >
            <UIcon name="i-lucide-sparkles" class="size-3.5" />
            Most Popular
          </div>

          <!-- Accent top line -->
          <div
            class="pointer-events-none absolute inset-x-0 -top-px h-px"
            :class="plan.mostPopular === 1 ? 'bg-gradient-to-r from-transparent via-indigo-300/70 to-transparent' : 'bg-gradient-to-r from-transparent via-white/30 to-transparent'"
          />

          <!-- Plan header -->
          <div class="flex flex-col gap-2">
            <h2 class="text-2xl font-semibold text-white">{{ plan.name }}</h2>
            <p class="mt-2 flex items-baseline gap-2">
              <span class="text-5xl font-extrabold tracking-tight text-white">
                ${{ plan.price.toFixed(2) }}
              </span>
              <span class="text-sm text-slate-400">/ month</span>
            </p>
            <p class="text-sm text-slate-400">
              {{ plan.description }}
            </p>
          </div>

          <!-- CTA -->
          <div class="mt-6">
            <button
              v-if="plan.isActive === 1"
              :disabled="true"
              class="flex w-full cursor-default items-center justify-center rounded-xl border border-white/10 bg-white/10 px-6 h-11 text-base font-semibold text-white"
            >
              <span class="truncate">Currently Using</span>
            </button>

            <button
              v-else
              class="flex h-11 w-full items-center justify-center gap-2 rounded-xl text-base font-semibold text-white transition
                     focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-0"
              :class="plan.packageType === 'DEFAULT'
                ? 'bg-slate-400/60 cursor-not-allowed'
                : 'bg-indigo-600 hover:bg-indigo-700 cursor-pointer'"
              :disabled="plan.packageType === 'default'"
              @click="onUpgradePackage(plan.subscriptionPlanId)"
            >
              <UIcon name="i-lucide-rocket" class="size-5" />
              <span class="truncate">Upgrade</span>
            </button>
          </div>

          <!-- Feature list -->
          <ul class="mt-8 space-y-3 border-t border-white/10 pt-6 text-sm text-slate-200">
            <li v-if="plan.requestsPerDay" class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>{{ plan.requestsPerDay }} Requests Per Day</span>
            </li>

            <li v-if="plan.advancedFeatures === 0" class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Basic Features</span>
            </li>

            <li v-if="plan.communitySupport === 1" class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Community Support</span>
            </li>

            <li v-if="!plan.requestsPerDay" class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Unlimited Requests</span>
            </li>

            <li v-if="plan.advancedFeatures === 1" class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Advanced Features</span>
            </li>

            <li v-if="plan.prioritySupport === 1" class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Priority Support</span>
            </li>
          </ul>

          <!-- Soft bottom glow -->
          <div class="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-white/10 to-transparent" />
        </div>
      </section>
    </div>

    <!-- Loader overlay for upgrade -->
    <PageLoader v-if="loadingUpgradePackage" />
  </main>
</template>
<script setup lang="ts">
import axios from "axios";
import PageLoader from "~/components/ui/PageLoader.vue";
import type { SubscriptionPlan } from "~/types/SubscriptionPlan";
const { $modal } = useNuxtApp();

const router = useRouter();
const loading = ref(true);
let subscriptionPlans = reactive<SubscriptionPlan[]>([]);
const config = useRuntimeConfig();
const errorMessage = ref<string | null>(null);
const loadingUpgradePackage = ref(false);

useSeoMeta({
  // Core
  title: 'Keetlo Pricing – AI HTML Generator To Build Faster with AI',
  description:
    'Simple, transparent pricing for Keetlo. Start free and upgrade for higher daily requests, advanced features, and priority support.',
  robots: 'index,follow,max-image-preview:large',

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/pricing`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Keetlo Pricing – AI HTML Generator To Build Faster with AI',
  ogDescription:
    'Choose a plan that fits your workflow. Generate HTML + Tailwind pages with live preview, remix templates, and export instantly.',
  ogImage: `${config.public.SITE_URL}/images/og/pricing.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Keetlo Pricing – AI HTML Generator To Build Faster with AI',
  twitterDescription:
    'From Free to Pro: pick the plan that lets you generate more pages, unlock advanced features, and get priority support.',
  twitterImage: `${config.public.SITE_URL}/images/og/pricing.png`,
})


onMounted(async () => {
  try {
    const response = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/subscription/info`
    );
    subscriptionPlans = response.data.subscriptionPlans;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
    }
  } finally {
    loading.value = false;
  }
});

const onUpgradePackage = async (subscriptionPlanId: string) => {
  try {
 const ok = await $modal.confirm({
  title: 'Confirm',
  html: '<p>Would you like to upgrade the package?</p>',
  confirmText: 'Upgrade now',
  cancelText: 'Cancel',
  variant: 'default'
})
if (!ok){return;}

    loadingUpgradePackage.value = true;
    const payload = {
      subscriptionPlanId: subscriptionPlanId,
    };
    const response = await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/stripe/create-payment`,
      payload
    );
    if (response.data.sessionUrl) {
      window.location.href = response.data.sessionUrl;
    }
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      if (error.status === 401) {
        router.push(`/auth/login?next=${window.location.href}`);
        return;
      }
      await $modal.alert({ 
      title: 'Error', 
      html: `<p>${toErrorMessage(error?.response?.data?.message)}</p>`, 
      variant: 'danger' 
    });
    }
  } finally {
    loadingUpgradePackage.value = false;
  }
};
</script>
