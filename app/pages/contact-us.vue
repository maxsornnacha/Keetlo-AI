<template>
  <main>
    <div class="mx-auto max-w-4xl">
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold tracking-tight text-white sm:text-5xl">
          Contact Us
        </h1>
        <p class="mt-4 text-lg leading-8 text-[#92adc9]">
          We're here to help! Reach out to our team for any questions or support
          you may need.
        </p>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-16">
        <form class="space-y-6" @submit.prevent="onSubmit">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div>
              <label class="sr-only" for="name">Your Name</label>
              <input
                id="name"
                v-model="form.name"
                maxlength="255"
                autocomplete="name"
                class="px-4 py-2 form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-md text-white bg-[#161B22] border border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB] focus:ring-opacity-50 h-11 placeholder:text-gray-500 px-3 font-normal leading-normal transition-colors"
                name="name"
                placeholder="Your Name"
                type="text"
                required
              >
            </div>
            <div>
              <label class="sr-only" for="email">Your Email</label>
              <input
                id="email"
                v-model="form.email"
                maxlength="255"
                autocomplete="email"
                class="px-4 py-2 form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-md text-white bg-[#161B22] border border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB] focus:ring-opacity-50 h-11 placeholder:text-gray-500 px-3 font-normal leading-normal transition-colors"
                name="email"
                placeholder="Your Email"
                type="email"
                required
              >
            </div>
          </div>

          <div>
            <label class="sr-only" for="subject">Subject</label>
            <input
              id="subject"
              v-model="form.subject"
              maxlength="255"
              class="px-4 py-2 form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-md text-white bg-[#161B22] border border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB] focus:ring-opacity-50 h-11 placeholder:text-gray-500 px-3 font-normal leading-normal transition-colors"
              name="subject"
              placeholder="Subject"
              type="text"
            >
          </div>

          <div>
            <label class="sr-only" for="message">Your Message</label>
            <textarea
              id="message"
              v-model="form.message"
              maxlength="500"
              class="px-4 py-2 form-input flex w-full min-w-0 flex-1 rounded-md text-white bg-[#161B22] border border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB] focus:ring-opacity-50 min-h-[140px] placeholder:text-gray-500 px-3 font-normal leading-normal transition-colors"
              name="message"
              placeholder="Your Message"
              rows="6"
              required
            />
          </div>

          <div class="flex items-center justify-between gap-3">
            <p v-if="errorMessage" class="text-sm text-red-400">
              {{ errorMessage }}
            </p>
            <button
              class="flex w-full lg:w-auto min-w-[120px] cursor-pointer items-center justify-center overflow-hidden rounded-md h-11 px-6 bg-indigo-600 text-white text-sm font-semibold leading-normal tracking-[0.015em] hover:bg-indigo-700/90 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
              type="submit"
              :disabled="loading || (!form.name || !form.email || !form.subject || !form.message)"
            >
              <span v-if="!loading">Send Message</span>
              <span v-else>Sending…</span>
            </button>
          </div>
        </form>

        <div class="space-y-8">
          <div>
            <h2 class="text-xl font-semibold leading-7 text-white">
              Other Ways to Reach Us
            </h2>
            <p class="mt-2 text-[#92adc9]">
              You can also contact us directly via email. We aim to respond to
              all inquiries within 24 hours.
            </p>
            <a
              class="mt-4 inline-flex items-center gap-2 text-[#1173d4] hover:text-[#1173d4]/90 font-medium"
              href="mailto:keetlo.ai@gmail.com"
            >
              <span class="material-symbols-outlined"> mail </span>
              keetlo.ai@gmail.com
            </a>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
<script setup lang="ts">
import axios from "axios";
import type { ContactMessage } from "~/types/ContactMessage";

const config = useRuntimeConfig();
useSeoMeta({
  // Core
  title: "Contact Keetlo – Support & Sales",
  description:
    "Need help with Keetlo? Our support team can assist with product questions, account and billing, and implementation guidance. Send us an email—most inquiries receive a reply within 24 hours.",
  robots: "index,follow,max-image-preview:large",
  // Open Graph
  ogUrl: `${config.public.SITE_URL}/contact-us`,
  ogSiteName: "Keetlo",
  ogType: "website",
  ogTitle: "Contact Keetlo – Support & Sales",
  ogDescription:
    "Reach the Keetlo team for product questions, billing, and technical support. We usually respond within 24 hours.",
  ogImage: `${config.public.SITE_URL}/images/og/contact-us.png`,

  // Twitter
  twitterCard: "summary_large_image",
  twitterTitle: "Contact Keetlo – Support & Sales",
  twitterDescription:
    "Email us with product or billing questions. The Keetlo team typically responds within 24 hours.",
  twitterImage: `${config.public.SITE_URL}/images/og/contact-us.png`,
});

const { $modal } = useNuxtApp();
const form = reactive<ContactMessage>({
  name: "",
  email: "",
  subject: "",
  message: "",
});

const loading = ref(false)
const errorMessage = ref<string | null>(null)

const onSubmit = async () => {
  errorMessage.value = null

  if (!form.name || !form.email || !form.subject ||  !form.message) {
    errorMessage.value = 'Please fill in name, email, subject and message.'
    return
  }

  loading.value = true
  try {
    const payload = {
      name: form.name,
      email: form.email,
      subject: form.subject,
      message: form.message,
    }

    await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/contact-message/create`, payload);
     await $modal.alert({ 
      title: 'Project successfully craeted!', 
      html: `<p>Thanks! Your message has been sent.</p>`, 
      variant: 'success' 
    });

    form.name = ''
    form.email = ''
    form.subject = ''
    form.message = ''
  } catch (error: unknown) {
    // try to surface a readable message
    if(axios.isAxiosError(error)){
    const msg =
      error?.response?.data?.message ||
      'Something went wrong. Please try again.'
    errorMessage.value = msg;
    } else {
       errorMessage.value = 'Something went wrong. Please try again.';
    }
  } finally {
    loading.value = false
  }
}

</script>
