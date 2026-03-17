import React, { useState, useEffect, useRef, useCallback } from 'react';
import { db } from '../db/db';
import DreamCard from '../components/DreamCard';
import { Search, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const PAGE_SIZE = 15;

const DreamLog = () => {
  const [dreams, setDreams] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const navigate = useNavigate();
  const observer = useRef();

  const loadDreams = async (isReset = false) => {
    const currentPage = isReset ? 0 : page;
    let query = db.local_dreams.orderBy('date').reverse();

    if (searchQuery.trim() !== '') {
      // Basic text filtering locally. In a real app full text search might be needed, but for local Dexie:
      const allMatches = await query.filter(d => d.text.toLowerCase().includes(searchQuery.toLowerCase())).toArray();
      const paginated = allMatches.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);
      if (isReset) { setDreams(paginated); } 
      else { setDreams(prev => [...prev, ...paginated]); }
      setHasMore(paginated.length === PAGE_SIZE);
    } else {
      const paginated = await query.offset(currentPage * PAGE_SIZE).limit(PAGE_SIZE).toArray();
      if (isReset) { setDreams(paginated); } 
      else { setDreams(prev => [...prev, ...paginated]); }
      setHasMore(paginated.length === PAGE_SIZE);
    }
  };

  useEffect(() => {
    setPage(0);
    setHasMore(true);
    loadDreams(true);
  }, [searchQuery]);

  useEffect(() => {
    if (page > 0) loadDreams(false);
  }, [page]);

  const lastElementRef = useCallback(node => {
    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        setPage(prev => prev + 1);
      }
    });
    if (node) observer.current.observe(node);
  }, [hasMore]);

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100vh', padding: '24px' }}>
      
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px', gap: '16px' }}>
         <button 
           onClick={() => navigate('/')} 
           style={{ background: 'transparent', padding: '8px', border: 'none', color: 'var(--text-main)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
         >
           <ArrowLeft size={24} />
         </button>
         <h1 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 600 }}>Rüya Günlüğü</h1>
      </div>

      <div style={{ position: 'relative', marginBottom: '24px' }}>
        <Search size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
        <input 
          type="text" 
          placeholder="Rüyalarda ara..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{ paddingLeft: '40px' }}
        />
      </div>

      <div style={{ flex: 1, overflowY: 'auto', paddingRight: '4px', display: 'flex', flexDirection: 'column' }}>
        {dreams.length === 0 ? (
           <div style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: '48px' }}>
              Sonuç bulunamadı...
           </div>
        ) : (
          dreams.map((dream, index) => {
            if (dreams.length === index + 1) {
              return <div ref={lastElementRef} key={dream.id}><DreamCard dream={dream} /></div>;
            } else {
              return <DreamCard key={dream.id} dream={dream} />;
            }
          })
        )}
      </div>
    </div>
  );
};

export default DreamLog;
