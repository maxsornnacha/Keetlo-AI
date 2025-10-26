<template>
  <main>
    <div class="mx-auto max-w-4xl">
      <div class="mb-10">
        <h1 class="text-2xl font-bold text-black tracking-tight">
          Terms and Conditions
        </h1>
        <p class="mt-2 text-gray-400">Last updated: October 26, 2023</p>
      </div>
      <div class="space-y-8 text-gray-400">
        <div>
          <h2 class="text-xl font-semibold text-black mb-4">
            1. Acceptance of Terms
          </h2>
          <p class="leading-relaxed">
            By accessing or using Keetlo AI, you agree to be bound by these
            Terms and Conditions. If you do not agree to all of these terms, do
            not use the platform. We reserve the right to update or modify these
            terms at any time without prior notice.
          </p>
        </div>
        <div>
          <h2 class="text-xl font-semibold text-black mb-4">
            2. User Responsibilities
          </h2>
          <p class="leading-relaxed">
            You are responsible for maintaining the confidentiality of your
            account and password. You agree not to use the platform for any
            unlawful or prohibited activities, including but not limited to
            generating malicious code or violating any applicable laws. You are
            solely responsible for all activities that occur under your account.
          </p>
        </div>
        <div>
          <h2 class="text-xl font-semibold text-black mb-4">
            3. Intellectual Property
          </h2>
          <p class="leading-relaxed">
            All content and materials available on Keetlo AI, including but
            not limited to software, designs, and documentation, are the
            property of Keetlo AI or its licensors and are protected by
            copyright, trademark, and other intellectual property laws. You may
            not use, copy, reproduce, modify, or distribute any of the content
            without our prior written consent.
          </p>
        </div>
        <div>
          <h2 class="text-xl font-semibold text-black mb-4">
            4. Limitation of Liability
          </h2>
          <p class="leading-relaxed">
            Keetlo AI is provided 'as is' without any warranties, express or
            implied. In no event shall Keetlo AI be liable for any direct,
            indirect, incidental, special, or consequential damages arising out
            of or in connection with your use of the platform. This includes,
            but is not limited to, damages for loss of profits, data, or other
            intangible losses.
          </p>
        </div>
        <div>
          <h2 class="text-xl font-semibold text-black mb-4">
            5. Modifications to Terms
          </h2>
          <p class="leading-relaxed">
            Keetlo AI reserves the right to modify these Terms and Conditions
            at any time. Your continued use of the platform after any such
            changes constitutes your acceptance of the new terms. We encourage
            you to review this page periodically for the latest information on
            our terms.
          </p>
        </div>
        <div>
          <h2 class="text-xl font-semibold text-black mb-4">
            6. Governing Law
          </h2>
          <p class="leading-relaxed">
            These Terms and Conditions shall be governed by and construed in
            accordance with the laws of the jurisdiction in which Keetlo AI
            is established, without regard to its conflict of law provisions.
            Any legal suit, action, or proceeding arising out of or related to
            these Terms and Conditions shall be instituted exclusively in the
            federal or state courts of that jurisdiction.
          </p>
        </div>
      </div>
    </div>
  </main>
</template>
<script setup lang="ts">
const config = useRuntimeConfig();
useSeoMeta({
  // Core
  title: 'Keetlo Terms of Service - Keetlo Ai Acceptable Use Policy',
  description:
    'Read the Keetlo Terms and Conditions. Learn about your responsibilities, acceptable use, intellectual property, limitations of liability, and governing law.',
  robots: 'index,follow,max-image-preview:large',

  // Canonical
  ogUrl: `${config.public.SITE_URL}/terms-of-service`,
  ogSiteName: 'Keetlo',
  ogType: 'website',

  // Open Graph
  ogTitle: 'Keetlo Terms of Service - Keetlo Ai Acceptable Use Policy',
  ogDescription:
    'The official Terms and Conditions for using Keetlo. Review acceptable use, IP ownership, limitations of liability, and legal jurisdiction.',
  ogImage: `${config.public.SITE_URL}/images/og/terms-of-service.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Keetlo Terms of Service - Keetlo Ai Acceptable Use Policy',
  twitterDescription:
    'Review Keetlo’s Terms and Conditions covering acceptable use, IP, liability limits, and governing law.',
  twitterImage: `${config.public.SITE_URL}/images/og/terms-of-service.png`,
})

</script>