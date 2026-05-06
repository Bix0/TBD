import React from 'react';

function RaidFilterBar({ rolFiltro, setRolFiltro, ilvlFiltro, setIlvlFiltro }) {
  return (
    <div style={{ display: 'flex', gap: '20px', alignItems: 'center', backgroundColor: '#1a1a1a', padding: '15px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #333' }}>
      
      {/* Filtro por Rol */}
      <div>
        <label style={{ marginRight: '10px', color: '#aaa', fontWeight: 'bold' }}>Selecciona un rol:</label>
        <select 
          value={rolFiltro} 
          onChange={(e) => setRolFiltro(e.target.value)} 
          style={{ padding: '8px', borderRadius: '4px', backgroundColor: '#242424', color: 'white', border: '1px solid #555', cursor: 'pointer' }}
        >
          <option value="Todos">Todos</option>
          <option value="Tanque">Tanque</option>
          <option value="Healer">Healer</option>
          <option value="DPS">DPS</option>
        </select>
      </div>

      {/* Filtro por Item Level  */}
      <div>
        <label style={{ marginRight: '10px', color: '#aaa', fontWeight: 'bold' }}>Item level mínimo:</label>
        <input 
          type="number" 
          min="0"
          max="500"
          value={ilvlFiltro} 
          onChange={(e) => setIlvlFiltro(Number(e.target.value))} 
          style={{ padding: '8px', borderRadius: '4px', backgroundColor: '#242424', color: 'white', border: '1px solid #555', width: '80px' }}
        />
      </div>

    </div>
  );
}

export default RaidFilterBar;
