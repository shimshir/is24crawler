import React from 'react'
import {Render} from 'jumpsuit'

import App from './components/app'

import {SearchState} from './components/search';
import {ResultsState} from "./components/results";

const globalState = {search: SearchState, results: ResultsState};

Render(globalState, <App/>);
