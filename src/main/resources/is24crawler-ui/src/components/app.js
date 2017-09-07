import React from 'react'
import {Router, Route, Component} from 'jumpsuit'
import 'bootstrap/dist/css/bootstrap.css'

import Start from './start'

export default Component(
    {
        render() {
            return (
                <div className="container">
                    <Router>
                        <Route path="/" component={Start}/>
                        <Route path="*" component={() => <h1>404 Not Found</h1>}/>
                    </Router>
                </div>
            )
        }
    }
);
