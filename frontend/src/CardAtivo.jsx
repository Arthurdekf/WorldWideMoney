import { useState, useEffect, useMemo } from 'react';
import { LineChart, Line, ResponsiveContainer, YAxis, Tooltip } from 'recharts';

// Sub-componente do Gráfico
const AtivoChart = ({ data, cor }) => {
    // Estado para controlar se o gráfico pode renderizar
    const [renderizar, setRenderizar] = useState(false);

    useEffect(() => {
        // Pequeno delay de 100ms para o Grid do CSS se estabilizar
        const timer = setTimeout(() => setRenderizar(true), 100);
        return () => clearTimeout(timer);
    }, []);

    if (!data || data.length === 0 || !renderizar) {
        return (
            <div style={{
                height: 80,
                marginTop: '20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexDirection: 'column'
            }}>
                <span style={{ color: '#666', fontSize: '1.0rem', fontWeight: 'bold' }}>
                    Mercado Fechado
                </span>
                <span style={{ color: '#444', fontSize: '0.9rem', marginTop: '6px' }}>
                    Dados intradiários indisponíveis aos finais de semana
                </span>
            </div>
        );
    }

    return (
        <div style={{
            width: '100%',
            height: 80,
            marginTop: '20px',
            position: 'relative' // Ajuda o ResponsiveContainer a se localizar
        }}>
            <ResponsiveContainer width="99%" height="100%">
                <LineChart data={data}>
                    <YAxis hide={true} domain={['auto', 'auto']} />
                    <Tooltip
                        contentStyle={{
                            backgroundColor: 'rgba(0,0,0,0.8)',
                            border: 'none',
                            borderRadius: '8px',
                            fontSize: '10px'
                        }}
                        itemStyle={{ color: cor }}
                        labelStyle={{ display: 'none' }}
                    />
                    <Line
                        type="monotone"
                        dataKey="preco"
                        stroke={cor}
                        strokeWidth={2}
                        dot={false}
                        isAnimationActive={true}
                        animationDuration={800}
                        style={{ filter: `drop-shadow(0px 0px 5px ${cor})` }} // Aumentei um pouco para ficar mais elegante
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
};

// Componente Principal do Card
const CardAtivo = ({ ativo, moeda, cotacaoDolar }) => {
    const [historico, setHistorico] = useState([]);
    const [periodo, setPeriodo] = useState(() => {
        // Busca o que está salvo. Se for null (primeira vez), usa '1d'
        const salvo = localStorage.getItem(`periodo_${ativo.simbolo}`);
        return salvo ? salvo : '1d';
    });

    useEffect(() => {
        localStorage.setItem(`periodo_${ativo.simbolo}`, periodo);
    }, [periodo, ativo.simbolo]);

    // 1. Detecta se a lista veio vazia (mercado fechado ou sem dados)
    const mercadoFechado = historico.length === 0;

    // Calcula os preços apenas se o mercado não estiver fechado
    const primeiroPreco = !mercadoFechado ? historico[0].preco : 0;
    const ultimoPreco = !mercadoFechado ? historico[historico.length - 1].preco : 0;

    // Cor: Cinza se fechado, Verde se subiu, Vermelho se caiu
    const corGrafico = mercadoFechado
        ? '#6b7280'
        : (ultimoPreco >= primeiroPreco ? '#22c55e' : '#ef4444');

    // Cálculo da variação percentual
    const variacao = (!mercadoFechado && primeiroPreco !== 0)
        ? ((ultimoPreco - primeiroPreco) / primeiroPreco) * 100
        : 0;

    const sinal = variacao >= 0 ? '+' : '';

    // Busca o histórico sempre que o período mudar
    useEffect(() => {
        fetch(`http://localhost:8080/api/ativos/historico/${ativo.simbolo}?periodo=${periodo}`)
            .then(res => res.json())
            .then(data => {
                setHistorico(data); // Salva o dado bruto que vem do Java (sempre em BRL)
            })
            .catch(err => console.error("Erro ao buscar histórico:", err));
    }, [periodo, ativo.simbolo]);

    // Se for USD, ele mapeia o histórico convertendo os valores. 
    // Se for BRL, ele usa o histórico direto.
    const dadosParaOGrafico = useMemo(() => {
        if (moeda === 'USD' && cotacaoDolar) {
            return historico.map(ponto => ({
                ...ponto,
                preco: ponto.preco / cotacaoDolar
            }));
        }
        return historico;
    }, [historico, moeda, cotacaoDolar]);

    return (
        <div style={{
            background: 'rgba(255, 255, 255, 0.05)',
            backdropFilter: 'blur(20px)',
            WebkitBackdropFilter: 'blur(20px)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            padding: '30px 25px',
            borderRadius: '32px',
            textAlign: 'left',
            boxShadow: '0 20px 40px rgba(0,0,0,0.4)'
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: '#3b82f6', fontWeight: 'bold', fontSize: '0.8rem', letterSpacing: '2px' }}>
                    {ativo.simbolo}
                </span>
                {<div style={{ display: 'flex', gap: '5px' }}>
                    {['1d', '7d', '1mo', '6mo', '1y', '5y'].map((p) => (
                        <button
                            key={p}
                            onClick={() => setPeriodo(p)}
                            style={{
                                background: periodo === p ? 'rgba(59, 130, 246, 0.2)' : 'transparent',
                                border: 'none',
                                color: periodo === p ? '#3b82f6' : '#444',
                                fontSize: '0.65rem',
                                cursor: 'pointer',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                fontWeight: 'bold',
                                transition: 'all 0.2s'
                            }}
                        >
                            {p.toUpperCase()}
                        </button>
                    ))}
                </div>}
            </div>

            <h2 style={{
                fontSize: '2.2rem',
                margin: '15px 0',
                fontWeight: '300',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between', // Joga a porcentagem para a direita
                flexWrap: 'wrap' // Garante que em telas pequenas não quebre
            }}>
                <div>
                    <span style={{ fontSize: '1.1rem', verticalAlign: 'middle', marginRight: '8px', color: '#3b82f6', fontWeight: '600' }}>
                        {moeda === 'BRL' ? 'R$' : 'U$'}
                    </span>
                    {new Number(moeda === 'BRL' ? ativo.precoAtual : ativo.precoAtual / cotacaoDolar).toLocaleString(moeda === 'BRL' ? 'pt-BR' : 'en-US', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    })}
                </div>

                {/* Badge de Variação Percentual ou Mercado Fechado */}
                <span style={{
                    fontSize: '0.85rem',
                    fontWeight: '700',
                    color: corGrafico,
                    background: `${corGrafico}15`, // Fundo suave com a cor do gráfico
                    padding: '4px 12px',
                    borderRadius: '12px',
                    marginLeft: '10px',
                    border: `1px solid ${corGrafico}30` // Borda sutil
                }}>
                    {mercadoFechado ? 'Fechado' : `${variacao >= 0 ? '+' : ''}${variacao.toFixed(2)}%`}
                </span>
            </h2>

            <p style={{ color: '#888', fontSize: '0.85rem', margin: '0 0 15px 0' }}>{ativo.nome}</p>

            {/* Renderiza o gráfico dentro do card */}
            <AtivoChart data={dadosParaOGrafico} cor={corGrafico} />
        </div>
    );
};

export default CardAtivo;