import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';

import Login from './components/Login';
import Personajes from './pages/Personajes';
import Raids from './pages/Raids'; 
import Inventario from './pages/Inventario';
import Ranking from './pages/Ranking';
import PanelAdmin from './pages/PanelAdmin';
import Facciones from './pages/Facciones';
import HistorialLoot from './pages/HistorialLoot'; 

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    if (token && username) {
      setUser({ 
        token, 
        username, 
        rol: localStorage.getItem('rol'), 
        id: parseInt(localStorage.getItem('userId')) 
      });
    }
  }, []);

  useEffect(() => {
    const interceptor = axios.interceptors.request.use(config => {
      const token = localStorage.getItem('token');
      if (token) config.headers.Authorization = `Bearer ${token}`;
      return config;
    });
    return () => axios.interceptors.request.eject(interceptor);
  }, [user]);

  if (!user) {
    return <Login onLoginSuccess={setUser} />;
  }

  return (
    <BrowserRouter>
      
      <Routes>
        <Route path="/personajes" element={<Personajes />} />
        <Route path="/raids" element={<Raids />} />
        <Route path="/inventario" element={<Inventario />} />
        <Route path="/ranking" element={<Ranking />} />
        <Route path="/facciones" element={<Facciones />} />
        <Route path="/historial" element={<HistorialLoot />} />
        <Route path="/admin" element={<PanelAdmin />} />
        
        <Route path="*" element={<Navigate to="/personajes" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;