import { useEffect, useState } from 'react'
import CardAtivo from './CardAtivo';

function App() {
  const [ativos, setAtivos] = useState([]);
  const [moeda, setMoeda] = useState('BRL');
  const [cotacaoDolar, setCotacaoDolar] = useState(null);
  const [girando, setGirando] = useState(false);

  // FUNÇÃO ÚNICA PARA ATUALIZAR TUDO
  const carregarDados = () => {
    setGirando(true);
    setTimeout(() => setGirando(false), 600); // Para a animação após 0.6s
    console.log("Atualizando dados...");

    // 1. Busca Ativos
    fetch("http://localhost:8080/api/ativos/dashboard")
      .then(res => res.json())
      .then(data => setAtivos(data))
      .catch(err => console.error("Erro ao carregar ativos:", err));

    // 2. Busca Dólar
    fetch('https://economia.awesomeapi.com.br/json/last/USD-BRL')
      .then(response => response.json())
      .then(data => {
        const precoDolar = parseFloat(data.USDBRL.bid);
        setCotacaoDolar(precoDolar);
      })
      .catch(error => console.error("Erro ao buscar dólar:", error));
  };

  // useEffect que chama a função ao carregar e ativa o pooling
  useEffect(() => {
    carregarDados();
    const interval = setInterval(carregarDados, 180000); // 30 segundos
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{
      backgroundColor: '#000',
      height: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      color: 'white',
      padding: '60px 50px 0 50px',
      fontFamily: 'sans-serif',
      position: 'relative',
      overflow: 'hidden' 
    }}>

      {/* Esferas de Luz de Fundo */}
      <div style={{ position: 'absolute', top: '10%', left: '15%', width: '400px', height: '400px', background: 'rgba(59, 130, 246, 0.15)', filter: 'blur(100px)', borderRadius: '50%', zIndex: 0 }}></div>
      <div style={{ position: 'absolute', bottom: '10%', right: '15%', width: '350px', height: '350px', background: 'rgba(147, 51, 234, 0.15)', filter: 'blur(100px)', borderRadius: '50%', zIndex: 0 }}></div>

      <header style={{ zIndex: 1, textAlign: 'center', marginBottom: '30px', flexShrink: 0 }}>
        <h1 style={{ fontSize: '3.5rem', fontWeight: '900', letterSpacing: '-2px', margin: 0 }}>
          <span style={{ color: '#3b82f6' }}>WorldWide</span>Money
        </h1>
        <p style={{ color: '#666', marginTop: '10px' }}>Dashboard de Inteligência Financeira</p>
      </header>

      {cotacaoDolar === null && (
        <div style={{
          width: '100%',
          maxWidth: '1100px',
          textAlign: 'center',
          marginBottom: '10px',
          color: '#3b82f6',
          fontSize: '0.8rem',
          fontWeight: 'bold',
          letterSpacing: '1px'
        }}>
          CONECTANDO ÀS BOLSAS...
        </div>
      )}

      <div style={{
        marginBottom: '30px',
        display: 'flex',
        justifyContent: 'flex-end',
        alignItems: 'center',
        gap: '15px',
        width: '100%',
        maxWidth: '1100px',
        zIndex: 1,
        flexShrink: 0
      }}>

        <button
          onClick={carregarDados}
          style={{
            background: 'rgba(255, 255, 255, 0.05)',
            backdropFilter: 'blur(10px)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            color: 'white',
            height: '42px',
            width: '42px',
            borderRadius: '14px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 0,
            overflow: 'hidden' // ícone não sai do limite ao girar
          }}
        >
          <span className={girando ? 'spinning' : ''} style={{
            fontSize: '1.6rem',
            display: 'flex', // giro 
            lineHeight: '1',
            marginTop: '-4px' // centralizar o ⟳
          }}>
            ⟳
          </span>
        </button>

        <div style={{
          background: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(10px)',
          padding: '4px',
          borderRadius: '14px',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          display: 'flex',
          gap: '4px',
          height: '42px', // Altura forçada igual ao botão de refresh
          boxSizing: 'border-box',
          alignItems: 'center'
        }}>
          {/* Opção REAL */}
          <button
            onClick={() => setMoeda('BRL')}
            style={{
              fontFamily: 'inherit',
              padding: '8px 16px',
              borderRadius: '10px',
              border: 'none',
              background: moeda === 'BRL' ? '#474e5a' : 'transparent',
              color: moeda === 'BRL' ? 'white' : 'rgba(255,255,255,0.5)',
              cursor: 'pointer',
              fontWeight: '600',
              fontSize: '0.75rem',
              letterSpacing: '0.5px',
              transition: 'all 0.3s ease'
            }}
          >
            R$ REAL
          </button>

          {/* Opção DOLAR */}
          <button
            onClick={() => setMoeda('USD')}
            style={{
              fontFamily: 'inherit',
              padding: '8px 16px',
              borderRadius: '10px',
              border: 'none',
              background: moeda === 'USD' ? '#474e5a' : 'transparent',
              color: moeda === 'USD' ? 'white' : 'rgba(255,255,255,0.5)',
              cursor: 'pointer',
              fontWeight: '600',
              fontSize: '0.75rem',
              letterSpacing: '0.5px',
              transition: 'all 0.3s ease'
            }}
          >
            U$ DOLAR
          </button>
        </div>
      </div>

      <div className="custom-scrollbar" style={{
        width: '100%',
        maxWidth: '1100px',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: '30px',
        overflowY: 'auto', // Habilita o scroll aqui
        paddingRight: '20px',
        paddingBottom: '100px',
        zIndex: 1,
        flex: 1 // Faz o grid ocupar o resto da altura da tela
      }}>

        {ativos.map(ativo => (
          <CardAtivo
            key={ativo.simbolo}
            ativo={ativo}
            moeda={moeda}
            cotacaoDolar={cotacaoDolar}
          />
        ))}
      </div>
      <style>{`
        /* 1. Esconde a barra do site todo */
        body {
          overflow: hidden;
          margin: 0;
        }

        /* 2. Animação de Giro para o Refresh */
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }

        .spinning {
          animation: spin 0.6s ease-in-out;
        }

        /* 3. Barra do Grid Inteligente (Só aparece no Hover) */
        .custom-scrollbar::-webkit-scrollbar {
          width: 10px;
        }

        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
          margin: 10px;
        }

        /* Estado normal: invisível */
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: transparent; 
          border-radius: 10px;
          border: 2px solid transparent;
          background-clip: content-box;
          transition: background 0.3s ease;
        }

        /* Estado Hover: aparece a barra */
        .custom-scrollbar:hover::-webkit-scrollbar-thumb {
          background: rgba(255, 255, 255, 0.15); 
        }

        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: #3b82f6 !important; /* Fica azul ao tocar nela */
        }

        /* Ajuste para Firefox */
        .custom-scrollbar {
          scrollbar-width: thin;
          scrollbar-color: transparent transparent;
          transition: scrollbar-color 0.3s;
        }
        .custom-scrollbar:hover {
          scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
        }
      `}</style>
    </div>
  )
}

export default App