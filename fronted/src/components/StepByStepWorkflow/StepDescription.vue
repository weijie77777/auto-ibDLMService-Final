<template>
  <p class="step-description">{{ text }}</p>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    required: true
  }
});
</script>

<style scoped>
.step-description {
  font-size: 18px;
  color: black;
  margin-top: -20px;
  margin-bottom: 20px;
  font-weight: 500;
  line-height: 1.4;
  max-width: 896px;
  text-align: justify;
}
</style>
