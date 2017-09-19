import React from 'react'
import {Render, State} from 'jumpsuit'

import App from './components/app'

import {SearchState} from './components/search';
import {ResultsState} from "./components/results";

const BackendDataState = State(
    {
        initial: {
            locations: []
        },
        setLocations(state, locations) {
            return {...state, locations};
        }
    }
);

const globalState = {search: SearchState, results: ResultsState, backendData: BackendDataState};

Render(globalState, <App/>);
