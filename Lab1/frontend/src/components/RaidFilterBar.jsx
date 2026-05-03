import react from 'react';

const Raidfilterbar = ({Rolfiltro, setRolfiltro, Ilvfiltro, setIlvfiltro}) => {

    return (
        <div style={{ padding: '1rem', borderBottom: '2px solid #333', marginBottom: '20px' }}> 
            <h2> Buscador de Raids </h2>

            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                <div>
                    <label> Selecciona un rol: </label>
                    <select value={Rolfiltro} onChange={(e) => setRolfiltro(e.target.value)}>
                        <option value=""> Todos </option>
                        <option value="DPS"> DPS </option>
                        <option value="Tank"> Tank </option>
                        <option value="Healer"> Healer </option>
                    </select>
                </div>

                <div>
                    <label> Item level minimo: {Ilvfiltro} </label>
                    <input 
                    type="range"
                    min="0"
                    max="200"
                    value={Ilvfiltro}
                    onChange={(e) => setIlvfiltro(e.target.value)}
                    />
                </div> 
            </div>
        </div>
    );
}

export default Raidfilterbar;
