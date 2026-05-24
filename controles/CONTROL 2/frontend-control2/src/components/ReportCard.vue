<template>
    <div class="card report-card" :class="{ highlight: highlight }">
        <h2>{{ title }}</h2>
        <slot />
    </div>
</template>

<script setup>
defineProps({
    title: { type: String, required: true },
    highlight: { type: Boolean, default: false },
});
</script>

<style scoped>
.card {
    background: var(--code-bg);
    border: 1px solid var(--border);
    padding: 24px;
    border-radius: 12px;
    margin-bottom: 24px;
    box-shadow: var(--shadow);
}

.report-card h2 {
    margin: 0 0 12px;
    font-size: 16px;
}

.highlight {
    border-left: 4px solid var(--accent);
}
</style>
