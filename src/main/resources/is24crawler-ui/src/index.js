import React from 'react'
import {Render} from 'jumpsuit'

import App from './components/app'

import {SearchState} from './components/search';

const globalState = {search: SearchState};

Render(globalState, <App/>);
