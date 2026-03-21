import React, { useState, useEffect } from 'react';
import { FlashcardEngine } from '../services/FlashcardEngine';
import { db } from '../db/db';

const engine = new FlashcardEngine(db);

const AnalysisWaitingState = ({ isReportReady, onReveal }) => {
  const [flashcard, setFlashcard] = useState(null);

  useEffect(() => {
    let intervalId;
    
    const loadNextCard = async () => {
      const card = await engine.getNextFlashcard();
      setFlashcard(card);
    };

    // Load initial card
    loadNextCard();

    // Rotate every 6 seconds if report is NOT ready yet
    if (!isReportReady) {
      intervalId = setInterval(() => {
        loadNextCard();
      }, 6000);
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [isReportReady]);

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px',
      gap: '32px',
      width: '100%',
      animation: isReportReady ? 'none' : 'breathe 4s infinite ease-in-out'
    }}>
      <style>
        {`
          @keyframes breathe {
            0% { transform: scale(0.95); opacity: 0.8; }
            50% { transform: scale(1.05); opacity: 1; }
            100% { transform: scale(0.95); opacity: 0.8; }
          }
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
          }
        `}
      </style>

      {/* Breathing Dreamcatcher Mock */}
      <div style={{
        width: '120px',
        height: '120px',
        borderRadius: '50%',
        border: '4px solid var(--primary)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        boxShadow: '0 0 20px var(--primary-glow)'
      }}>
        <span style={{ fontSize: '2rem' }}>🕸️</span>
      </div>

      {/* Flashcard Area */}
      {flashcard && (
        <div style={{
          backgroundColor: 'var(--surface)',
          padding: '24px',
          borderRadius: 'var(--radius-lg)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
          maxWidth: '400px',
          textAlign: 'center',
          animation: 'fadeIn 0.5s ease-out'
        }}>
          <p style={{ 
            fontSize: '0.85rem', 
            color: 'var(--primary)', 
            fontWeight: 'bold', 
            textTransform: 'uppercase',
            marginBottom: '8px'
          }}>
            {flashcard.category}
          </p>
          <p style={{ fontSize: '1.1rem', color: 'var(--text-main)', lineHeight: '1.5' }}>
            "{flashcard.text}"
          </p>
        </div>
      )}

      {/* Reveal Button when ready */}
      {isReportReady ? (
        <button 
          onClick={onReveal}
          style={{
            marginTop: '16px',
            backgroundColor: 'var(--primary)',
            color: 'white',
            padding: '16px 24px',
            borderRadius: 'var(--radius-full)',
            border: 'none',
            fontSize: '1.1rem',
            fontWeight: 'bold',
            cursor: 'pointer',
            boxShadow: '0 0 15px var(--primary-glow)',
            animation: 'fadeIn 0.8s ease-out'
          }}
        >
          Your Subconscious Summary is Ready - Reveal
        </button>
      ) : (
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Analyzing Subconscious Patterns...
        </p>
      )}
    </div>
  );
};

export default AnalysisWaitingState;
