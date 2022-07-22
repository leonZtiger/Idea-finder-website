import React, { useState } from 'react'
import classes from './home.module.scss';
import { AiOutlineSearch } from 'react-icons/ai';
import { AiOutlineMinus, AiOutlineClose } from 'react-icons/ai';
import { BiSquare } from 'react-icons/bi';
import axios from 'axios';

const api = axios.create({
    baseURL: `http://127.0.0.1:8080`
})

const Home = () => {

    const [searchTerms, setSearchTerms] = useState("");
    const [originality, setOriginality] = useState(0);
    const [purschable, setPurschable] = useState(0);
    const [inProduction, setInProduction] = useState(0);
    const [loading, setLoading] = useState(false);
    const [text, setText] = useState("");
    const [score, setScore] = useState("");

    function search() {
        //check that search terms exist else warn the user that they need to type
        if (searchTerms.length !== 0) {
            //starts all the loading animation and zero-out all the scores
            setLoading(true);
            setOriginality(0);
            setInProduction(0);
            setPurschable(0);
            animateCMD();
            //posts GET-request to the server and awaits the results
            api.get(`/?search=${searchTerms}`).then(res => {

                //console.log(res.data.results[0]);
                //turn of the loading screen
                setLoading(false);
                //write the results to the score
                increement(setOriginality, res.data.results[0].org, 0)
                increement(setPurschable, res.data.results[0].pur, 0)
                increement(setInProduction, res.data.results[0].prod, 0)
                //sets emoji based on the score  
                setScores(res.data.results[0].org);

            })
        } else
            alert("please type a idea in the search bar")
    }
    //animation for the score
    const increement = (setState, endNum, startNum) => {
        setTimeout(() => {

            setState(startNum);
            if (startNum < endNum) {
                startNum++
                increement(setState, endNum, startNum);
            }
            //gives a non linear and more random increement for looks/feel
        }, (Math.random() * 10) + 40);
    }
    //updates the search value
    const updateSearch = (e) => {
        if (e.length > 50)
            alert("max 40 chaachters")
        else
            setSearchTerms(e)
    }
    //animation for the fake cmd
    function animateCMD() {
        //get and open file
        var rawFile = new XMLHttpRequest();
        rawFile.open("GET", './cmdText.txt', false);

        rawFile.onreadystatechange = function () {
            //4 means the get request is sent and responded is recieved
            if (rawFile.readyState === 4) {
                //200 and 0 means the file is readable
                if (rawFile.status === 200 || rawFile.status === 0) {
                    var allText = rawFile.responseText.split('\r\n');
                    // alert(allText);
                    writeText(0, "");

                    function writeText(i, temp) {
                        setTimeout(() => {
                            //scrolls the textarea down so you can see all the new lines
                            var textArea = document.getElementById('text');
                            textArea.scrollTop = textArea.scrollHeight;

                            if (i < allText.length) {
                                //add new line to the fake cmd
                                temp += '\r\n' + allText[i];
                                i++
                                //sets the text in the fake cmd
                                setText(temp)
                                //calls it self to make a loop
                                writeText(i, temp);
                            }
                        }, 180)
                    }
                }
            }
        }
        //no need to send data
        rawFile.send(null);
    }
    //gives a emoji based on the score
    function setScores(i) {

        if (i < 10) {
            setScore('💩');
            return;
        }
        if (i < 20) {
            setScore('😑');
            return;
        }
        if (i < 30) {
            setScore('🤨');
            return;
        }
        if (i < 50) {
            setScore('🤔');
            return;
        }
        if (i > 70) {
            setScore('😌');
            return;
        }
    }

    return (
        <div className={classes.maincon}>
            {
                /* if loading is true show the loading screen */
                loading ?
                    <div className={classes.filter}>
                        <h1>Loading..</h1>
                        {/*the fake cmd*/}
                        <div className={classes.cmd}>
                            <div className={classes.cmd_bar}>
                                <ul>
                                    <li>
                                        <AiOutlineMinus />
                                    </li>
                                    <li>
                                        <BiSquare />
                                    </li>
                                    <li>
                                        <AiOutlineClose />
                                    </li>
                                </ul>
                            </div>
                            <textarea id="text" className={classes.cmd_text} value={text}>

                            </textarea>

                        </div>
                    </div>
                    : null
            }
            {/* */}
            <div className={classes.intro}>
                <h1>The originality checker🔍</h1>
            </div>

            <div className={classes.content}>
                {/* search cotainer */}
                <div className={classes.searchbarCon}>

                    <input placeholder={"type some idea..."} value={searchTerms} onChange={(e) => updateSearch(e.target.value)} />

                    <button onClick={() => search()}>
                        <AiOutlineSearch className={classes.icon} />
                    </button>
                </div>
                {/*the results*/}
                <div className={classes.resultsCon}>

                    <div className={classes.result}>
                        <h1>{purschable}%</h1>
                        <p>purschable</p>
                    </div>

                    <div className={classes.result}>
                        <h1>{originality}%</h1>
                        <p>originality</p>
                        <h1>{score}</h1>
                    </div>

                    <div className={classes.result}>
                        <h1>{inProduction}%</h1>
                        <p>in production</p>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Home;