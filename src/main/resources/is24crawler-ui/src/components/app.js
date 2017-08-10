import React from 'react'
import {Router, Route, Component} from 'jumpsuit'
import 'bootstrap/dist/css/bootstrap.css'

import {Search} from './search'
import Results from './results'

export default Component(
    {
        render() {
            return (
                <div className="container">
                    <Router>
                        <Route path="/" component={Search}/>
                        <Route path="/results" component={Results}/>
                        <Route path="*" component={() => <h1>404 Not Found</h1>}/>
                    </Router>
                </div>
            )
        }
    }
);
