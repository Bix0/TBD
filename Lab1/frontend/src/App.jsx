import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Raids from './pages/Raids';

function App() {
  return (
    <Router>
      <Routes>
        {/* Si alguien entra a la raíz "/", lo mandamos directo al login */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        {/* Nuestras dos pantallas principales */}
        <Route path="/login" element={<Login />} />
        <Route path="/raids" element={<Raids />} />
      </Routes>
    </Router>
  );
}

export default App;