<!-- eslint-disable vue/no-v-html -->
<template>
  <div class="p-4 max-w-4xl mx-auto">
    <h1 class="text-2xl font-bold mb-4">AI Page Generator</h1>

    <textarea
      v-model="userInput"
      placeholder="Type your input..."
      rows="4"
      class="w-full p-2 border rounded mb-4"
    />

    <button
      class="text-white px-4 py-2 rounded mb-4"
      :class="loadingSubmit ? 'bg-indigo-400' : 'cursor-pointer bg-indigo-600 hover:bg-indigo-700'"
      :disabled="loadingSubmit"
      @click="startStream"
    >
      <span v-if="loadingSubmit">Loading...</span>
      <span v-else>Generate Pages</span>
    </button>

    <h2 class="text-xl font-semibold mb-2">Streaming Response:</h2>
    <div class="bg-gray-800 p-2 rounded max-h-96 overflow-auto break-words text-white">
      <pre>{{ streamContent }}</pre>
    </div>

    <div  v-for="webPage in webPages"  :key="webPage?.path">
      <h2 class="text-xl font-semibold mt-6 mb-2">Preview HTML: {{ webPage?.path }}</h2>
    <iframe
      v-if="webPage?.htmlContent"
      :srcdoc="webPage?.htmlContent"
      class="w-full min-h-96 border rounded bg-white"
   />
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRuntimeConfig } from "#app";
import type { WebPage } from "@/modules/pageStream";
import { streamPages } from "@/modules/pageStream";

const config = useRuntimeConfig();
const userInput = ref(`To design a comprehensive website for a book rental service , we might consider the following pages : - Homepage : The main landing page showcasing the service , its benefits , and a call to action ( e . g ., browse books , register ).
   - Book Catalog : A searchable and filter able database of available books , with details like title , author , genre , condition , and price .
   - User Registration / Login : Pages for new users to create accounts and existing users to log in .
   - Book Details Page : A dedicated page for each book , displaying detailed information , images , and reviews .
   - Rental Management : A section for users to manage their rentals , including tracking due dates , making payments , and requesting renewals .
   - Payment Gateway : Integration with secure payment processors for handling rental fees and late payment penalties .
   - Contact Us : A form or page for users to contact the service with inquiries or support requests .
   - About Us : Information about the company , its mission , and team .
   - FAQ : Frequently Asked Questions to address common user queries .
   - Blog ( Optional ): A blog section for book reviews , recommendations , and updates . To create a successful website for book rental , consider user experience , mobile responsiveness , and secure payment options .`);
const loadingSubmit = ref(false);
const streamContent = ref("");
const webPages = reactive<WebPage[]>([]);

async function startStream() {
  loadingSubmit.value = true;
  streamContent.value = "";
  webPages.splice(0);
  const token = getToken();

  await streamPages(
    userInput.value,
    config.public.NUXT_PUBLIC_API_BASE,
    token? token : "",
    (chunk) => {
      streamContent.value += chunk;
    },
    (page) => {
      if (!webPages.find((p) => p.path === page.path)) {
        webPages.push(page);
      }
    }
  );

  loadingSubmit.value = false;

  // TODO: here call your API to store webPages into DB
}
</script>


<style scoped>
textarea {
  font-family: monospace;
}
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
