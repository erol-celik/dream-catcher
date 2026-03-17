import React from 'react';
import { CloudOff, CheckCircle2, Frown, Meh, Smile } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const getSentimentIcon = (sentiment) => {
  switch (sentiment) {
    case 'NIGHTMARE': return <Frown size={20} color="#EF4444" />;
    case 'GREAT': return <Smile size={20} color="#10B981" />;
    case 'NEUTRAL':
    default: return <Meh size={20} color="#94A3B8" />;
  }
};

const SyncStatusIcon = ({ isSynced }) => {
  if (isSynced) {
    return <CheckCircle2 size={16} color="var(--success)" />;
  }
  return <CloudOff size={16} color="var(--text-muted)" title="Buluta yükleniyor..." />;
};

const DreamCard = ({ dream }) => {
  const navigate = useNavigate();
  const dateObj = new Date(dream.date);
  const formattedDate = dateObj.toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' });

  return (
    <div 
      onClick={() => navigate(`/dream/${dream.id}`)}
      style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--radius-md)',
        padding: '16px',
        marginBottom: '16px',
        cursor: 'pointer',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        transition: 'transform 0.1s',
      }}
      onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
      onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{formattedDate}</span>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          {getSentimentIcon(dream.sentiment)}
          <SyncStatusIcon isSynced={dream.is_synced === 1} />
        </div>
      </div>
      <p style={{ 
        color: 'var(--text-main)', 
        fontSize: '1rem', 
        lineHeight: '1.5',
        display: '-webkit-box',
        WebkitLineClamp: 2,
        WebkitBoxOrient: 'vertical',
        overflow: 'hidden',
        margin: 0
      }}>
        {dream.text}
      </p>
    </div>
  );
};

export default DreamCard;
