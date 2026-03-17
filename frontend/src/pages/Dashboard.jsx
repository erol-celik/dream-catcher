import React from 'react';
import Header from '../components/Header';
import StreakWidget from '../components/StreakWidget';
import DailyActionCTA from '../components/DailyActionCTA';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const navigate = useNavigate();
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '24px' }}>
      <Header />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
        <StreakWidget />
      </div>
      <DailyActionCTA />
      
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
    </div>
  );
};

export default Dashboard;
