import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Axios request interceptor to attach the JWT token to every outgoing request
api.interceptors.request.use(
  (config) => {
    // Retrieve the guest or authenticated user's token from local storage
    const token = localStorage.getItem('dream_token');
    if (token) {
      // Add the token to the Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
