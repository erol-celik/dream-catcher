import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { db } from '../db/db';
import { ArrowLeft, Sparkles, Frown, Meh, Smile } from 'lucide-react';
import api from '../api/axios';

const sleepFacts = [
  "Rüyaların %90'ı uyandıktan sonraki 10 dakika içinde unutulur...",
  "İnsanlar hayatlarının yaklaşık 6 yılını rüya görerek geçirir...",
  "Kör doğan insanlar, rüyalarında sadece ses ve koku duyarlar...",
  "Beyin uyku sırasında tüm bedeni güvenlik için felç eder...",
  "Rüyalarda sadece gerçek hayatta gördüğümüz yüzleri görürüz...",
];

const FactRotatingLoader = () => {
  const [factIndex, setFactIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setFactIndex((prev) => (prev + 1) % sleepFacts.length);
    }, 3500);
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(15, 23, 42, 0.85)',
      backdropFilter: 'blur(8px)',
      display: 'flex', flexDirection: 'column',
      justifyContent: 'center', alignItems: 'center',
      zIndex: 9999,
      padding: '24px',
      textAlign: 'center'
    }}>
      <div className="spinner" style={{
        width: '48px', height: '48px',
        border: '4px solid var(--surface)',
        borderTopColor: 'var(--primary)',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite',
        marginBottom: '24px'
      }} />
      <p style={{
        color: 'var(--text-main)',
        fontSize: '1.2rem',
        fontWeight: 500,
        opacity: 0.9,
        transition: 'opacity 0.5s ease-in-out',
        minHeight: '60px'
      }}>
        {sleepFacts[factIndex]}
      </p>

      <style>{`
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
};

const getSentimentIcon = (sentiment) => {
  switch (sentiment) {
    case 'NIGHTMARE': return <Frown size={24} color="#EF4444" />;
    case 'GREAT': return <Smile size={24} color="#10B981" />;
    case 'NEUTRAL':
    default: return <Meh size={24} color="#94A3B8" />;
  }
};

const DreamDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [dream, setDream] = useState(null);
  const [isLoadingAnalysis, setIsLoadingAnalysis] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [analysisError, setAnalysisError] = useState(null);

  useEffect(() => {
    const fetchDream = async () => {
      const found = await db.local_dreams.get(parseInt(id, 10));
      if (found) setDream(found);
    };
    fetchDream();
  }, [id]);

  const handleAIAnalysis = async () => {
    if (!dream) return;
    setIsLoadingAnalysis(true);
    setAnalysisError(null);

    try {
      // Real API call to backend AI analysis endpoint
      const response = await api.post(`/dreams/${dream.id}/analyze`, {
        content: dream.text
      });

      const result = response.data;
      // Backend returns DreamAnalysisResult { tags: [...], sentiment: "..." }
      setAnalysisResult({
        meaning: result.sentiment 
          ? `AI Analiz Sonucu: Genel duygu durumu "${result.sentiment}" olarak belirlendi.`
          : 'Analiz tamamlandı.',
        archetypes: result.tags || []
      });
    } catch (error) {
      console.error('AI Analysis failed:', error);
      
      if (error.response?.status === 404) {
        // Dream not synced to backend yet; use local dream id as hint
        setAnalysisError('Bu rüya henüz buluta senkronize edilmedi. Lütfen senkronizasyonun tamamlanmasını bekleyin.');
      } else if (error.response?.status === 401) {
        setAnalysisError('Oturum geçersiz. Lütfen uygulamayı yeniden başlatın.');
      } else {
        setAnalysisError('Analiz sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
      }
    } finally {
      setIsLoadingAnalysis(false);
    }
  };

  if (!dream) return <div style={{ padding: '24px' }}>Yükleniyor...</div>;

  const dateObj = new Date(dream.date);
  const formattedDate = dateObj.toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric', weekday: 'long' });

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: '100vh', padding: '24px' }}>
      {isLoadingAnalysis && <FactRotatingLoader />}
      
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px', justifyContent: 'space-between' }}>
         <button 
           onClick={() => navigate(-1)} 
           style={{ background: 'transparent', padding: '8px', border: 'none', color: 'var(--text-main)' }}
         >
           <ArrowLeft size={24} />
         </button>
         <div>{getSentimentIcon(dream.sentiment)}</div>
      </div>

      <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>{formattedDate}</p>

      <div style={{ 
        flex: 1, 
        color: 'var(--text-main)', 
        fontSize: '1.15rem', 
        lineHeight: '1.8',
        whiteSpace: 'pre-wrap',
        marginBottom: '40px'
      }}>
        {dream.text}
      </div>

      {analysisError && (
        <div style={{
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          borderRadius: 'var(--radius-md)',
          padding: '16px',
          marginBottom: '16px',
          color: '#EF4444',
          fontSize: '0.95rem',
          textAlign: 'center'
        }}>
          {analysisError}
        </div>
      )}

      {!analysisResult ? (
        <button 
          onClick={handleAIAnalysis}
          disabled={isLoadingAnalysis}
          style={{
            width: '100%',
            padding: '16px',
            backgroundColor: 'transparent',
            border: '2px solid var(--primary)',
            color: 'var(--primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            position: 'relative',
            overflow: 'hidden'
          }}
          className="shimmer-btn"
        >
          <Sparkles size={20} />
          Yapay Zeka ile Analiz Et
          <style>{`
            .shimmer-btn::after {
              content: '';
              position: absolute;
              top: 0;
              left: -100%;
              width: 50%;
              height: 100%;
              background: linear-gradient(to right, transparent, rgba(107, 70, 193, 0.2), transparent);
              transform: skewX(-20deg);
              animation: shimmer 3s infinite;
            }
            @keyframes shimmer {
              0% { left: -100%; }
              100% { left: 200%; }
            }
          `}</style>
        </button>
      ) : (
        <div style={{
          backgroundColor: 'var(--surface)',
          borderRadius: 'var(--radius-lg)',
          padding: '24px',
          borderLeft: '4px solid var(--primary)'
        }}>
          <h3 style={{ margin: '0 0 16px 0', color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
             <Sparkles size={20} /> Analiz Sonucu
          </h3>
          <p style={{ color: 'var(--text-main)', fontSize: '1.05rem', lineHeight: '1.6', margin: '0 0 16px 0' }}>
            {analysisResult.meaning}
          </p>
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            {analysisResult.archetypes.map((arc, idx) => (
              <span key={idx} style={{ padding: '4px 12px', backgroundColor: 'var(--background)', borderRadius: '999px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                #{arc}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default DreamDetail;
