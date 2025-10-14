<template>
  <main>
    <div class="mx-auto max-w-4xl">
      <div class="mb-12 text-center">
        <h1
          class="text-4xl md:text-5xl font-extrabold tracking-tighter text-white"
        >
          Privacy Policy
        </h1>
        <p class="mt-4 text-lg text-gray-400">Last updated: July 26, 2024</p>
      </div>
      <div class="space-y-10">
        <section>
          <h2
            class="text-2xl font-bold text-white border-l-4 border-indigo-500 pl-4 mb-4"
          >
            Data We Collect
          </h2>
          <p class="text-gray-400 leading-relaxed">
            We collect several types of information to provide and improve our
            services. This includes personal information such as your name,
            email address, and payment details, as well as usage data like your
            project configurations, code snippets, and interaction logs. We also
            gather device information, including IP addresses and browser types,
            to ensure optimal performance and security.
          </p>
        </section>
        <section>
          <h2
            class="text-2xl font-bold text-white border-l-4 border-indigo-500 pl-4 mb-4"
          >
            Purpose of Data Collection
          </h2>
          <p class="text-gray-400 leading-relaxed">
            The data we collect is used to facilitate code generation,
            personalize your experience, and enhance our platform's
            functionality. Specifically, we use your information to process
            payments, manage your projects, provide customer support, and
            communicate updates. Additionally, we analyze usage patterns to
            improve our algorithms and develop new features.
          </p>
        </section>
        <section>
          <h2
            class="text-2xl font-bold text-white border-l-4 border-indigo-500 pl-4 mb-4"
          >
            Data Sharing
          </h2>
          <p class="text-gray-400 leading-relaxed">
            We do not sell or rent your personal information to third parties.
            However, we may share data with trusted service providers who assist
            us in operating our platform, such as payment processors and cloud
            storage providers. These providers are contractually obligated to
            protect your information and only use it for the purposes we
            specify. We may also disclose information if required by law or to
            protect our rights and safety.
          </p>
        </section>
        <section>
          <h2
            class="text-2xl font-bold text-white border-l-4 border-indigo-500 pl-4 mb-4"
          >
            User Rights
          </h2>
          <p class="text-gray-400 leading-relaxed">
            You have the right to access, correct, or delete your personal
            information. You can manage your account settings to update your
            details or contact our support team for assistance. You may also
            object to the processing of your data or request data portability.
            We will respond to your requests within a reasonable timeframe.
          </p>
        </section>
        <section>
          <h2
            class="text-2xl font-bold text-white border-l-4 border-indigo-500 pl-4 mb-4"
          >
            Security Measures
          </h2>
          <p class="text-gray-400 leading-relaxed">
            We implement robust security measures to protect your data from
            unauthorized access, alteration, or disclosure. These measures
            include encryption, access controls, and regular security audits.
            While we strive to protect your information, no method of
            transmission over the internet is completely secure, and we cannot
            guarantee absolute security.
          </p>
        </section>
      </div>
    </div>
  </main>
</template>
<script setup lang="ts">
const config = useRuntimeConfig();
useSeoMeta({
  // Core
  title: 'Keetlo Privacy Policy – Data Collection, Use & Security',
  description:
    'Read how Keetlo collects, uses, and protects your data: personal information, usage logs, payment details, data sharing with processors, user rights (access, deletion, portability), and security practices.',
  robots: 'index,follow,max-image-preview:large',

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/privacy-policy`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Keetlo Privacy Policy – Data Collection, Use & Security',
  ogDescription:
    'Learn about Keetlo’s data practices: what we collect, why we collect it, how we share it, your rights, and how we secure your information.',
  ogImage: `${config.public.SITE_URL}/images/og/privacy-policy.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Keetlo Privacy Policy – Data Collection, Use & Security',
  twitterDescription:
    'Understand Keetlo’s approach to privacy—data collection, usage, sharing, user rights, and security measures.',
  twitterImage: `${config.public.SITE_URL}/images/og/privacy-policy.png`,
})

</script>
