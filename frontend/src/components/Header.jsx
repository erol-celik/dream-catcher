import React from 'react';
import { Moon } from 'lucide-react';

const Header = () => {
  return (
    <header style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', paddingBottom: '32px' }}>
      <Moon size={32} color="var(--primary)" />
      <h1 style={{ marginLeft: '12px', fontSize: '1.5rem', fontWeight: 700, letterSpacing: '0.05em' }}>
        DreamCatcher
      </h1>
    </header>
  );
};

export default Header;
