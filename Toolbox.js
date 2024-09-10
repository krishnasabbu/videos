/* eslint-disable react/display-name */
import React, { useState } from "react";
import { useEditor } from "@craftjs/core";
import { DatabaseIcon } from "@heroicons/react/outline";
import { Columns } from "../user/Columns";
import { Panel, PanelSection } from "./Panel";
import { Date, KafkaConsumer, KafkaProducer, ProjectFeature } from "../user";
import { Database } from "../user/Database";
import { CalendarIcon, CodeIcon, FlagIcon, KeyIcon } from "@heroicons/react/solid";
import { FaChevronUp, FaChevronDown } from "react-icons/fa";
import { SiApachekafka, SiDatefns, SiDatev, SiFastapi, SiMongodb, SiOracle } from "react-icons/si";

// ToolboxSection component
const ToolboxSection = ({ title, children, isOpen, onToggle }) => (
  <PanelSection>
    <div 
      className="flex justify-between items-center cursor-pointer" 
      onClick={onToggle}
      style={{
        backgroundColor: '#f0f0f0',
        padding: '10px',
        borderRadius: '5px',
		    boxSizing: 'border-box',
        marginTop: '10px',
        width: '100%',
      }}
    >
      <h2 style={{
        fontSize: '1rem',
        fontWeight: 'bold',
        margin: 0,
        flex: 1,
      }}>
        {title}
      </h2>
      <div style={{
        padding: '0 10px',
        display: 'flex',
        alignItems: 'center',
        borderLeft: '1px solid #d1d5db',
      }}>
        {isOpen ? <FaChevronUp size={12} color="#25292e" /> : <FaChevronDown size={12} color="#25292e" />}
      </div>
    </div>
    {isOpen && (
      <div className="grid grid-cols-3 gap-2">{children}</div>
    )}
  </PanelSection>
);

// Updated ToolboxButton with icon only and hover text
const ToolboxButton = React.forwardRef(({ icon, description }, ref) => (
  <div
    ref={ref}
    className="flex items-center justify-center p-2 rounded-lg transition-shadow duration-200 hover:shadow-md cursor-pointer"
    title={description}
    style={{
      backgroundColor: '#f9fafb',
      border: '1px solid #e5e7eb',
      borderRadius: '8px',
      marginTop: '10px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    {React.createElement(icon, { className: "text-indigo-500 w-6 h-6" })}
  </div>
));

export const Toolbox = () => {
  const { actions, connectors, query, canUndo, canRedo } = useEditor();
  const [searchTerm, setSearchTerm] = useState("");

  const [servicesOpen, setServicesOpen] = useState(true);
  const [kafkaServicesOpen, setKafkaServicesOpen] = useState(false);
  const [databaseOpen, setDatabaseOpen] = useState(false);
  const [restServiceOpen, setRestServicesOpen] = useState(false);
  const [patternsOpen, setPatternsOpen] = useState(false);
  const [businessComponentsOpen, setBusinessComponentsOpen] = useState(false);

  // Define sections and their components
  const sections = [
    {
      title: "Services",
      isOpen: servicesOpen,
      toggle: () => setServicesOpen(prev => !prev),
      components: [
        { id: 'columns', component: <Columns />, icon: CodeIcon, description: "Drag to add a new service" },
      ],
    },
    {
      title: "Kafka Services",
      isOpen: kafkaServicesOpen,
      toggle: () => setKafkaServicesOpen(prev => !prev),
      components: [
        { id: 'Kafka Consumer', component: <KafkaConsumer />, icon: SiApachekafka, description: "Drag to add a Oracle Client" },
        { id: 'Kafka Producer', component: <KafkaProducer />, icon: SiApachekafka, description: "Drag to add a Mongodb Client" },
      ],
    },
    {
      title: "Database",
      isOpen: databaseOpen,
      toggle: () => setDatabaseOpen(prev => !prev),
      components: [
        { id: 'Oracle', component: <KafkaConsumer />, icon: SiOracle, description: "Drag to add a Oracle Client" },
        { id: 'Mogo DB', component: <Database />, icon: SiMongodb, description: "Drag to add a Mongodb Client" },
      ],
    },
    {
      title: "Rest Services",
      isOpen: restServiceOpen,
      toggle: () => setRestServicesOpen(prev => !prev),
      components: [
        { id: 'Rest Template Client', component: <KafkaConsumer />, icon: SiFastapi, description: "Drag to add a Oracle Client" },
        { id: 'Fiegn Client', component: <Database />, icon: SiMongodb, description: "Drag to add a Mongodb Client" },
      ],
    },
    {
      title: "Business Components",
      isOpen: businessComponentsOpen,
      toggle: () => setBusinessComponentsOpen(prev => !prev),
      components: [
        { id: 'projectFeature', component: <ProjectFeature />, icon: FlagIcon, description: "Drag to add a Project Feature" },
        { id: 'date', component: <Date />, icon: SiDatev, description: "Drag to add a Date Picker" },
      ],
    },
    {
      title: "Patterns",
      isOpen: patternsOpen,
      toggle: () => setPatternsOpen(prev => !prev),
      components: [
        { id: 'Circut Breaker', component: <KafkaConsumer />, icon: SiApachekafka, description: "Drag to add a Circut Breaker" },
        { id: 'Transactional', component: <Database />, icon: DatabaseIcon, description: "Drag to add a Transactional" },
      ],
    },
  ];

  const filteredSections = sections
      .map(section => {
        const hasMatchingComponents = section.components.some(c =>
          c.id.toLowerCase().includes(searchTerm.toLowerCase())
        );
        return {
          ...section,
          isOpen: searchTerm ? hasMatchingComponents : section.isOpen,
          components: section.components.filter(c =>
            c.id.toLowerCase().includes(searchTerm.toLowerCase())
          ),
        };
      })
  .filter(section => section.components.length > 0);

  return (
    <Panel>
      <input
        type="text"
        placeholder="Search components..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        style={{
          width: '100%',
          padding: '8px',
          borderRadius: '4px',
          border: '1px solid #ccc',
          marginBottom: '10px',
          backgroundColor: '#f0f0f0',
          color: '#25292e', // Text color
        }}
      />
      {filteredSections.map(({ title, isOpen, components, toggle }, index) => (
        <ToolboxSection
          key={index}
          title={title}
          isOpen={isOpen}
          onToggle={toggle} // Pass the toggle function
        >
          {components.map(({ id, component, icon, description }) => (
            <ToolboxButton
              key={id}
              ref={(ref) => connectors.create(ref, component)}
              icon={icon}
              description={description}
            />
          ))}
        </ToolboxSection>
      ))}
    </Panel>
  );
};
