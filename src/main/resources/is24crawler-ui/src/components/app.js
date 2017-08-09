import React from 'react'
import {Router, Route, Component} from 'jumpsuit'

import {Search} from './search'
import Results from './results'

export default Component(
    {
        render() {
            return (
                <Router>
                    <Route path="/" component={Search}/>
                    <Route path="/results" component={Results}/>
                    <Route path="*" component={() => <h1>404 Not Found</h1>}/>
                </Router>
            )
        }
    }
);
