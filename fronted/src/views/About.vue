<template>
  <div class="about-page">
    <NavBar />
    <div class="bigcontainer">
      <div class="about-container">
        <h1>Developer Introduction</h1>
        <div v-for="(developer, index) in developers" :key="index" class="developer">
          <img :src="developer.profilePicture" :alt="developer.name"/>
          <div class="developer-info">
            <h2>{{ developer.name }}</h2>
            <p>{{ developer.role }}</p>
            <p>{{ developer.bio }}</p>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import NavBar from '@/components/NavBar.vue';
import Footer from '@/components/Footer.vue';
import developer1 from '@/assets/images/developer1.jpg';
import developer2 from '@/assets/images/developer2.jpg';
import developer3 from '@/assets/images/developer3.jpg';

const developers = ref([]);

const fallbackDevelopers = [
  {
    name: 'Developer 1',
    role: 'Lead Developer',
    bio: 'Expert in machine learning and data analysis',
    profilePicture: developer1
  },
  {
    name: 'Developer 2',
    role: 'Frontend Developer',
    bio: 'Specialized in React and modern web technologies',
    profilePicture: developer2
  },
  {
    name: 'Developer 3',
    role: 'Backend Developer',
    bio: 'Focused on API design and database optimization',
    profilePicture: developer3
  }
];

const loadDevelopers = async () => {
  try {
    // 使用 /developers 端点，通过 Vite 代理转发到后端
    const response = await fetch('api/developers');
    if (response.ok) {
      const data = await response.json();
      if (Array.isArray(data) && data.length > 0) {
        developers.value = data;
        return;
      }
    }
    // 如果 API 失败或返回空数据，使用默认数据
    developers.value = fallbackDevelopers;
  } catch (error) {
    console.error('Error fetching developers:', error);
    // 如果 API 失败，使用默认数据
    developers.value = fallbackDevelopers;
  }
};

onMounted(() => {
  loadDevelopers();
});
</script>

<style scoped>
.about-page {
  width: 100%;
  background-color: #fff;
}

.bigcontainer {
  margin-top: 70px;
}

.about-container {
  max-width: 800px;
  margin: 70px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}

.developer {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.developer img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin-right: 20px;
  object-fit: cover;
  flex-shrink: 0;
}

.developer-info {
  flex: 1;
}

.developer-info h2 {
  margin: 0 0 10px 0;
  font-size: 1.5em;
  color: #333;
}

.developer-info p {
  margin: 5px 0;
  color: #666;
}

.developer-info p:first-of-type {
  font-weight: 500;
  color: #555;
}
</style>

