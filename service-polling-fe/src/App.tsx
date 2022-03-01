import React from 'react';
import logo from './logo.png';
import './App.scss';
import { Header } from './components/header/Header';

function App() {
  return (
    <div className="main-component">
      <Header/>
      <img className="logo" src={logo} alt="logo"/>

    </div>
  );
}

export default App;
