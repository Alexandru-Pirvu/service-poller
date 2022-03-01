import { Button } from '@mui/material';
import { Link } from 'react-router-dom';
import React from 'react';

import './Header.scss';

export const Header = () => {
  return (
    <div className="header">
      <Link className="home-link" to="/">Service Poller</Link>

      <Button className="router-button" variant="contained">
        <Link className="router-link" to="/services">Services</Link>
      </Button>
    </div>
  );
};