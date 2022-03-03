import { useEffect, useState } from 'react';
import axios from 'axios';
import { Header } from '../../components/header/Header';
import Modal from 'react-modal';
import './services.scss';
import { Button } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';

import * as EventBus from '@vertx/eventbus-bridge-client.js/vertx-eventbus';

interface ServiceStatusUpdated {
  id: number;
  status: string;
}

const Services = () => {

  const [services, setServices] = useState<any[]>([]);
  const [showModal, setShowModal] = useState<boolean>(false);

  const [name, setName] = useState<string>();
  const [url, setUrl] = useState<string>();
  const [urlValid, setUrlValid] = useState<boolean>(false);

  const axiosInstance = axios.create({
    baseURL: process.env.REACT_APP_SERVER_URL ?? ''
  });

  useEffect(() => {
    Modal.setAppElement('body');

    axiosInstance.get(`/services`)
      .then(res => {
        setServices(res.data);
      });

    let eventBus = new EventBus('http://localhost:8080/eventbus');
    eventBus.onopen = () => {
      eventBus.registerHandler('services', (error: any, message: { body: ServiceStatusUpdated }) => {
        if (!error) {
          setServices(services => {
            const indexToUpdate = services.findIndex(service => service.id === message.body.id);
            const serviceToUpdate = services[indexToUpdate];

            serviceToUpdate.status = message.body.status;

            services.splice(indexToUpdate, 1, serviceToUpdate);

            return [...services];
          });
        }
      });
    };
  }, []);

  const addService = () => {
    setShowModal(false);

    console.log('name', name);
    console.log('url', url);

    axiosInstance.post(`/services`, { name, url })
      .then(res => {
        setServices([...services, res.data]);
        console.log(res.data);
      });
  };

  const deleteService = (serviceId: number) => {
    axiosInstance.delete(`/services/${serviceId}`)
      .then(res => {
        setServices(services.filter(service => service.id !== serviceId));
        console.log(res.data);
      });
  };

  const servicesRows = services.map(service => {
    const serviceStatusCellClass = service.status === 'OK' ? 'green': 'red';

    return (
      <tr key={service.id}>
        <td>{service.id}</td>
        <td>{service.name}</td>
        <td><a href={service.url}>{service.url}</a></td>

        <td className={`${serviceStatusCellClass} status`}/>
        <td>
          <DeleteIcon className="action-icon" onClick={() => deleteService(service.id)}>Filled</DeleteIcon>
        </td>
      </tr>
    );
  });

  return (
    <div>
      <Header/>

      <div className="services-container">

        <div className="table-header-container">
          <span>Services</span>
          <Button className="add-service-button" variant="outlined" color="secondary" onClick={() => setShowModal(true)}>Add new service</Button>
        </div>

        <table>
          <thead>
          <tr>
            <th>Id</th>
            <th>Name</th>
            <th>URL</th>
            <th>STATUS</th>
            <th/>
          </tr>
          </thead>
          <tbody>
          {servicesRows}
          </tbody>
        </table>
      </div>

      <Modal className="create-service-modal" isOpen={showModal}>
        <div className="modal-container">
          <form>
            <div className="form-inputs">
              <div className="form-group">
                <label htmlFor="name">Name</label>
                <input id="name" onInput={e => setName((e.target as HTMLInputElement).value)}/>
              </div>
              <div className="form-group">
                <label htmlFor="url">URL</label>
                <input id="url" type="url" onInput={e => {
                  const htmlInputElement: HTMLInputElement = e.target as HTMLInputElement;
                  setUrlValid(htmlInputElement.validity.valid);
                  setUrl(htmlInputElement.value);
                }}/>
              </div>
            </div>


            <div className="form-buttons">
              <Button className="form-button" variant="outlined" onClick={() => setShowModal(false)}>CANCEL</Button>
              <Button className="form-button" disabled={!urlValid} variant="contained" color="secondary" onClick={addService}>ADD</Button>
            </div>
          </form>
        </div>
      </Modal>
    </div>
  );
};

export default Services;