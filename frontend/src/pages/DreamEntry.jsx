import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { X, Frown, Meh, Smile } from 'lucide-react';
import { db } from '../db/db';
import { InputValidator } from '../utils/InputValidator';

const DreamEntry = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { editMode, existingDream } = location.state || {};

  const [text, setText] = useState(editMode ? existingDream.text : '');
  const [sentiment, setSentiment] = useState(editMode ? existingDream.sentiment : 'NEUTRAL'); 

  const handleSave = async () => {
    if (text.trim().length < 10) return; // Prevention heuristic

    const validation = InputValidator.validateDreamInput(text);
    if (!validation.isValid) {
      alert(validation.message); // Basic toast/alert for now
      return;
    }

    const tzOffset = (new Date()).getTimezoneOffset() * 60000;
    const localDate = new Date(Date.now() - tzOffset).toISOString().slice(0, 19);

    try {
      if (editMode && existingDream) {
        // UPDATE existing record for Delayed Recall (smart edit)
        await db.local_dreams.update(existingDream.id, {
          text,
          sentiment,
          is_synced: 0, // Flag for SyncManager to pick it up again
          type: 'DREAM' // In case we are overwriting something else, ensure it's a dream
        });
      } else {
        // CREATE new record
        const newDream = {
          clientId: crypto.randomUUID(),
          text,
          sentiment,
          is_synced: 0,
          date: localDate,
          type: 'DREAM'
        };
        await db.local_dreams.add(newDream);
      }
      navigate('/'); // Go back instantly
    } catch (err) {
      console.error('Failed to save to local DB:', err);
    }
  };

  const isSaveDisabled = text.trim().length < 10;

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100vh', backgroundColor: 'var(--background)' }}>
      {/* Header / Actions */}
      <div style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <button 
          onClick={() => navigate('/')} 
          style={{ background: 'transparent', padding: '8px', border: 'none', color: 'var(--text-muted)' }}
          aria-label="Cancel"
        >
          <X size={28} />
        </button>
        <button 
          className="primary" 
          onClick={handleSave}
          disabled={isSaveDisabled}
          style={{ padding: '8px 24px' }}
        >
          Kaydet
        </button>
      </div>

      {/* Main Text Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '0 24px' }}>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Zihninde kalanları buraya dök..."
          style={{
            flex: 1,
            backgroundColor: 'transparent',
            border: 'none',
            outline: 'none',
            resize: 'none',
            fontSize: '1.25rem',
            lineHeight: '1.6',
            color: 'var(--text-main)',
            padding: '16px 0'
          }}
          autoFocus
        />
      </div>

      {/* Sentiment Selector (Safe zone above keyboard) */}
      <div style={{ padding: '24px', backgroundColor: 'var(--surface)', borderTopLeftRadius: 'var(--radius-lg)', borderTopRightRadius: 'var(--radius-lg)' }}>
        <p style={{ color: 'var(--text-muted)', marginBottom: '16px', fontSize: '0.9rem', textAlign: 'center' }}>
           Rüyanın sende bıraktığı his
        </p>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '32px' }}>
          <SentimentButton 
             icon={<Frown size={32} />} 
             label="Kabus" 
             active={sentiment === 'NIGHTMARE'} 
             onClick={() => setSentiment('NIGHTMARE')} 
             color="#EF4444"
          />
          <SentimentButton 
             icon={<Meh size={32} />} 
             label="Nötr" 
             active={sentiment === 'NEUTRAL'} 
             onClick={() => setSentiment('NEUTRAL')} 
             color="#94A3B8"
          />
          <SentimentButton 
             icon={<Smile size={32} />} 
             label="Harika" 
             active={sentiment === 'GREAT'} 
             onClick={() => setSentiment('GREAT')} 
             color="#10B981"
          />
        </div>
      </div>
    </div>
  );
};

const SentimentButton = ({ icon, label, active, onClick, color }) => (
  <button 
    onClick={onClick}
    style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      background: 'transparent',
      border: 'none',
      padding: '8px',
      opacity: active ? 1 : 0.4,
      transform: active ? 'scale(1.1)' : 'scale(1)',
      transition: 'all 0.2s',
      color: active ? color : 'var(--text-main)'
    }}
  >
    {icon}
    <span style={{ fontSize: '0.8rem', marginTop: '8px', color: 'var(--text-main)' }}>{label}</span>
  </button>
);

export default DreamEntry;
