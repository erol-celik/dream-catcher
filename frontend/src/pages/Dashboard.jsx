import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import StreakWidget from '../components/StreakWidget';
import DailyActionCTA from '../components/DailyActionCTA';
import AnalysisWaitingState from '../components/AnalysisWaitingState';
import { useNavigate } from 'react-router-dom';
import { SyncManager } from '../services/SyncManager';
import { notificationService } from '../services/NotificationService';
import { liveQuery } from 'dexie';
import { db } from '../db/db';

const Dashboard = () => {
  const navigate = useNavigate();
  const [isGeneratingReport, setIsGeneratingReport] = useState(false);
  const [isReportReady, setIsReportReady] = useState(false);

  const [todayEntry, setTodayEntry] = useState(null);

  useEffect(() => {
    notificationService.requestPermissions();

    // Listen for AI triggers from SyncManager
    SyncManager.onAnalysisTriggered(() => {
      setIsGeneratingReport(true);
      setIsReportReady(false);
      
      setTimeout(() => {
        setIsReportReady(true);
      }, 15000); // 15 seconds mock wait
    });

    // liveQuery for today's entries to instantly update Priority UI
    const today = new Date().toISOString().split('T')[0];
    const subscription = liveQuery(() => 
      db.local_dreams.filter(dream => dream.date.startsWith(today)).toArray()
    ).subscribe({
      next: (entries) => {
        if (entries && entries.length > 0) {
          // Priority 1: Dream (type not DUMMY_FOCUS)
          const dream = entries.find(e => e.type !== 'DUMMY_FOCUS');
          if (dream) {
            setTodayEntry(dream);
          } else {
            // Priority 2: Dummy Focus
            const dummy = entries.find(e => e.type === 'DUMMY_FOCUS');
            setTodayEntry(dummy);
          }
        } else {
          setTodayEntry(null);
        }
      }
    });

    return () => subscription.unsubscribe();
  }, []);

  const handleReveal = () => {
    setIsGeneratingReport(false);
    setIsReportReady(false);
    navigate('/report'); // Redirect to some report view (assuming /report exists or will exist)
  };

  const handleFabClick = () => {
    if (todayEntry && todayEntry.type !== 'DUMMY_FOCUS') {
      // Edit mode
      navigate('/entry', { state: { editMode: true, existingDream: todayEntry } });
    } else {
      // Create mode (even if todayEntry is Dummy, we create a new Dream)
      navigate('/entry');
    }
  };

  if (isGeneratingReport) {
    return (
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100vh', backgroundColor: 'var(--background)' }}>
        <AnalysisWaitingState 
          isReportReady={isReportReady} 
          onReveal={handleReveal} 
        />
      </div>
    );
  }

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '24px', position: 'relative' }}>
      <Header />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
        <StreakWidget />
      </div>

      {todayEntry && todayEntry.type !== 'DUMMY_FOCUS' && (
        <div 
          onClick={handleFabClick}
          style={{
            marginTop: '32px', padding: '20px', backgroundColor: 'var(--surface)', 
            borderRadius: 'var(--radius-lg)', border: '1px solid var(--primary)', cursor: 'pointer'
          }}>
          <h3 style={{ color: 'var(--success)', marginBottom: '8px' }}>Today's Dream</h3>
          <p style={{ color: 'var(--text-muted)' }}>{todayEntry.text.slice(0, 50)}...</p>
        </div>
      )}

      {todayEntry && todayEntry.type === 'DUMMY_FOCUS' && (
        <div style={{
          marginTop: '32px', padding: '20px', backgroundColor: 'var(--surface)', 
          borderRadius: 'var(--radius-lg)', border: '1px solid var(--text-muted)'
        }}>
          <h3 style={{ color: 'var(--text-main)', marginBottom: '8px' }}>Today's Focus</h3>
          <p style={{ color: 'var(--text-muted)' }}>{todayEntry.text}</p>
        </div>
      )}

      {!todayEntry && <DailyActionCTA />}
      
      <button 
        onClick={() => navigate('/log')}
        style={{
          marginTop: '16px',
          backgroundColor: 'transparent',
          color: 'var(--text-muted)',
          fontSize: '1rem',
          textDecoration: 'underline',
          border: 'none',
          padding: '12px'
        }}
      >
        Geçmiş Rüyalara Göz At
      </button>

      {/* Floating Action Button for Delayed Recall */}
      <button className="fab" onClick={handleFabClick} aria-label="Log Dream">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
      </button>
    </div>
  );
};

export default Dashboard;
