'use strict';

{
    let pageOrchestrator = new PageOrchestrator();
    let playlistList = new PlaylistList();
    let showOnePlaylist = new ShowOnePlaylist();
    let createPlaylist = new CreatePlaylist();
    let addSongToPlaylist = new AddSongToPlaylist();
    let playlistPageTitle = new PlaylistPageTitle();
    let theActualPlaylist = new TheActualPlaylist();
    let deletePlaylist = new DeletePlaylist();
    let uploadSong = new UploadSong();
    let allSongs = new AllSongs();
    let songPage = new SongPage();
    let playlistOrdinator = new PlaylistOrdinator();
    let logout = new Logout();

    window.addEventListener("load", () => {
        pageOrchestrator.start();
    });

    function PlaylistList(){
        this.div = document.getElementById("playlistlist");

        this.init = function(){
            this.close();
            this.update();
        }
        this.close = function(){
            this.div.innerHTML = "";
            this.div.style.display = "none";
        }
        this.update = function(){
            makeCall("GET", 'GetHomePlaylists', null, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    let message = x.responseText;
                    switch(x.status){
                        case 200:
                            let response = JSON.parse(message);
                            response.forEach((playlist) => {
                                let a = document.createElement("a");
                                a.textContent = ("Playlist: " + playlist.name + " Creation date and time: " + formatDate(playlist.creationDateTime));
                                a.href = ("#");
                                a.addEventListener("click", (e) => {
                                    e.preventDefault();
                                    showOnePlaylist.show(playlist);
                                });
                                this.div.appendChild(a);

                                let orderButton = document.createElement("button");
                                orderButton.textContent = "Order";
                                orderButton.addEventListener("click", (e) => {
                                    e.preventDefault();
                                    playlistOrdinator.show(playlist.id);
                                });
                                this.div.appendChild(orderButton);

                                this.div.appendChild(document.createElement("br"));
                            });
                            this.div.style.display = "block";
                            break;
                        default:
                            alert("Can't load playlists");
                            break;
                    }
                }
            });

        }
    }

    function CreatePlaylist(){
        this.form = document.getElementById("createplaylistform");
        this.select = document.getElementById("createplaylistformselect");
        this.name = document.getElementById("chooseplaylistname");

        this.init = function(){
            this.close();
            this.show();
            this.form.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit();
            });
        }
        this.close = function(){
            this.form.style.display = "none";
        }
        this.open = function(){
            this.form.style.display = "block";
        }
        this.show = function(){
            this.select.innerHTML = "";
            makeCall("GET", 'GetAllSongs?getAlbumCovers=false', null, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    let message = x.responseText;
                    switch(x.status){
                        case 200:
                            let response = JSON.parse(message);
                            this.update(response);
                            break;
                        default:
                            alert("Can't load songs for playlist creation");
                            break;

                    }
                }
            });
            this.form.style.display = "block";
        }
        this.update = function(songs){
            songs.forEach((song) => {
                let option = document.createElement("option");
                option.value = song.id;
                option.multiple = true;
                option.textContent = song.title;
                this.select.appendChild(option);
            });
        }
        this.submit = function (){
            this.close();
            makeCall("POST", 'CreatePlaylist', this.form, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    switch(x.status){
                        case 200:
                            this.form.reset();
                            this.open();
                            playlistList.init();
                            break;
                        default:
                            alert("Can't create playlist");
                            break;
                    }
                }
            });
        }
    }

    function ShowOnePlaylist(){
        this.line = document.getElementById("line1");
        this.playlistpage = document.getElementById("playlistpage");
        this.closeplaylist = document.getElementById("closeplaylist");

        this.init = function(){
            this.closeplaylist.addEventListener("submit", (e) => {
                e.preventDefault();
                this.close();
            });
            this.close();
        }
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
        this.close = function(){
            this.line.style.display = "none";
            this.playlistpage.style.display = "none";
            playlistPageTitle.close();
            addSongToPlaylist.close();
            theActualPlaylist.close();
            deletePlaylist.close();
        }
        this.show = function(playlist){
            playlistOrdinator.close();
            this.line.style.display = "block";
            this.playlistpage.style.display = "block";
            this.update(playlist);
        }
        this.update = function(playlist){
            playlistPageTitle.reset(playlist);
            addSongToPlaylist.reset(playlist);
            theActualPlaylist.reset(playlist);
            deletePlaylist.reset(playlist);

        }
    }

    function AddSongToPlaylist(){
        this.addsongtoplaylistform = document.getElementById("addsongtoplaylistform");
        this.addsongtoplaylistformselect = document.getElementById("addsongtoplaylistformselect");
        this.hiddenplaylistid = document.getElementById("hiddenplaylistid");
        let currentPlaylist;

        this.init = function(){
            this.close();
            this.addsongtoplaylistform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit(currentPlaylist);
            });
        }
        this.close = function(){
            this.addsongtoplaylistform.style.display = "none";
        }
        this.reset = function(playlist){
            this.addsongtoplaylistformselect.innerHTML = "";
            this.showList(playlist);
        }
        this.showList = function(playlist){
            makeCall("GET", 'GetExcludedSongs?playlistId=' + playlist.id, null, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    let message = x.responseText;
                    switch(x.status){
                        case 200:
                            let response = JSON.parse(message);
                            response.forEach((song) => {
                                let option = document.createElement("option");
                                option.value = song.id;
                                option.textContent = song.title;
                                this.addsongtoplaylistformselect.appendChild(option);
                            });
                            this.hiddenplaylistid.value = playlist.id;
                            currentPlaylist = playlist;
                            this.addsongtoplaylistform.style.display = "block";
                            break;
                        default:
                            alert("Can't load songs to add to playlist");
                            break;
                    }
                }
            });
        }
        this.submit = function(playlist){
            this.close();
            makeCall("POST", 'AddSongsToPlaylist', this.addsongtoplaylistform, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    switch(x.status){
                        case 200:
                            this.addsongtoplaylistform.reset();
                            showOnePlaylist.reset(playlist);
                            break;
                        default:
                            alert("Can't add songs to playlist");
                            break;
                    }
                }
            });
        }
    }

    function PlaylistPageTitle(){
        let playlistpagetitle = document.getElementById("playlistpagetitle");

        this.init = function(){
            this.close();
        }
        this.close = function(){
            playlistpagetitle.style.display = "none";
        }
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
        this.show = function(playlist){
            playlistpagetitle.style.display = "block";
            playlistpagetitle.textContent = ("Playlist: " + playlist.name);
        }
    }

    function TheActualPlaylist(){
        this.pagenumber = document.getElementById("pagenumber");
        this.emptyplaylist = document.getElementById("emptyplaylist");
        this.songstable = document.getElementById("songstable");
        this.previouspagebutton = document.getElementById("previouspagebutton");
        this.nextpagebutton = document.getElementById("nextpagebutton");
        let currentPage = 1;
        let songList;
        let showingPlaylist;

        this.init = function(){
            this.close();
            this.previouspagebutton.addEventListener("click", (e) => {
                currentPage--;
                this.update(showingPlaylist, songList);
            });
            this.nextpagebutton.addEventListener("click", (e) => {
                currentPage++;
                this.update(showingPlaylist, songList);
            });
        }
        this.close = function(){
            this.pagenumber.style.display = "none";
            this.emptyplaylist.style.display = "none";
            this.songstable.style.display = "none";
            this.previouspagebutton.style.display = "none";
            this.nextpagebutton.style.display = "none";
            currentPage = 1;
            songList = null;
        }
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
        this.show = function(playlist){
            showingPlaylist = playlist;
            makeCall("GET", 'GetPlaylistSongs?playlistId=' + playlist.id, null, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    let message = x.responseText;
                    switch(x.status){
                        case 200:
                            let response = JSON.parse(message);
                            if(response.length > 0){
                                songList = response;
                                currentPage = 1;
                                this.update(playlist, response);
                            } else {
                                this.emptyplaylist.style.display = "block";
                            }
                            break;
                        default:
                            alert("Can't load playlist songs");
                            break;
                    }
                }
            });
        }
        this.update = function(playlist, songs){
            this.pagenumber.textContent = ("Page: " + currentPage);
            this.pagenumber.style.display = "block";

            let fiveSongs = songs.slice((currentPage - 1) * 5, currentPage * 5);

            this.songstable.innerHTML = "";
            fiveSongs.forEach((song) => {
                let th = document.createElement("th");
                let img = document.createElement("img");
                img.src = ("data:image/png;base64," + song.encodedAlbumCover);
                console.log(song.encodedAlbumCover);
                img.width = 100;
                th.appendChild(img);
                let br = document.createElement("br");
                th.appendChild(br);
                let a = document.createElement("a");
                a.href = ("#");
                a.textContent = song.title;
                a.addEventListener("click", (e) => {
                    e.preventDefault();
                    songPage.show(song.id);
                });
                th.appendChild(a);
                let form = document.createElement("form");
                let input = document.createElement("input");
                input.type = "hidden";
                input.name = "songId";
                input.value = song.id;
                form.appendChild(input);
                let input2 = document.createElement("input");
                input2.type = "hidden";
                input2.name = "playlistId";
                input2.value = playlist.id;
                form.appendChild(input2);
                let button = document.createElement("button");
                button.type = "submit";
                button.textContent = "Remove";
                button.addEventListener("click", (e) => {
                    e.preventDefault();
                    makeCall("POST", 'RemoveSongFromPlaylist', form, (x) => {
                        if (x.readyState == XMLHttpRequest.DONE) {
                            switch(x.status){
                                case 200:
                                    showOnePlaylist.reset(playlist);
                                    break;
                                default:
                                    alert("Can't remove song from playlist");
                                    break;
                            }
                        }
                    });
                });
                form.appendChild(button);
                th.appendChild(form);
                this.songstable.appendChild(th);
            });
            this.songstable.style.display = "block";

            if(currentPage == 1){
                this.previouspagebutton.style.display = "none";
            }
            else{
                this.previouspagebutton.style.display = "block";
            }
            if(fiveSongs.length < 5 || currentPage * 5 >= songs.length){
                this.nextpagebutton.style.display = "none";
            }
            else{
                this.nextpagebutton.style.display = "block";
            }
        }
    }

    function DeletePlaylist(){
        this.deleteplaylistform = document.getElementById("deleteplaylistform");

        this.init = function(){
            this.close();
            this.deleteplaylistform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit();
            });
        }
        this.close = function(){
            this.deleteplaylistform.style.display = "none";
        }
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
        this.show = function(playlist){
            this.deleteplaylistform.style.display = "block";
            this.deleteplaylistform.elements["playlistId"].value = playlist.id;
        }
        this.submit = function(){
            this.close();
            makeCall("POST", 'DeletePlaylist', this.deleteplaylistform, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            showOnePlaylist.close();
                            playlistList.init();
                            alert("Playlist deleted successfully!");
                            break;
                        default:
                            alert("Can't delete playlist");
                            break;
                    }
                }
            });
        }
    }

    function UploadSong(){
        this.uploadsongform = document.getElementById("uploadsongform");

        this.init = function(){
            this.uploadsongform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit();
            });
        }

        this.close = function(){
            this.uploadsongform.style.display = "none";
        }

        this.open = function(){
            this.uploadsongform.style.display = "block";
        }

        this.submit = function(){
            this.close();
            makeCall("POST", 'UploadSong', this.uploadsongform, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            this.open();
                            alert("Song uploaded successfully!");
                            createPlaylist.show();
                            break;
                        default:
                            this.open();
                            alert("Can't upload song");
                            break;
                    }
                }
            });
        }
    }

    function AllSongs(){
        let allsongslink = document.getElementById("allsongslink");
        let songstable = document.getElementById("allsongstable");
        let div = document.getElementById("allsongspage");
        let closeform = document.getElementById("closeallsongs");

        this.init = function(){
            songstable.style.display = "none";
            div.style.display = "none";
            allsongslink.addEventListener("click", (e) => {
                e.preventDefault();
                this.showAllSongs();
            });
            closeform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.close();
                closeform.style.display = "none";
            });
        }

        this.close = function(){
            songstable.style.display = "none";
            div.style.display = "none";
            songstable.innerHTML = "";

        }

        this.showAllSongs = function () {
            closeform.style.display = "block";
            makeCall("GET", 'GetAllSongs?getAlbumCovers=true', null, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    switch (x.status) {
                        case 200:
                            let response = JSON.parse(x.responseText);
                            if (response.length > 0) {
                                songstable.innerHTML = "";
                                response.forEach((song) => {
                                    let tr = document.createElement("tr");
                                    let th = document.createElement("th");
                                    let img = document.createElement("img");
                                    img.src = ("data:image/png;base64," + song.encodedAlbumCover);
                                    img.width = 100;
                                    th.appendChild(img);
                                    let br = document.createElement("br");
                                    th.appendChild(br);
                                    let newdiv = document.createElement("div");
                                    newdiv.textContent = song.title;
                                    th.appendChild(newdiv);
                                    let form = document.createElement("form");
                                    let input = document.createElement("input");
                                    input.type = "hidden";
                                    input.name = "songId";
                                    input.value = song.id;
                                    form.appendChild(input);
                                    let button = document.createElement("button");
                                    button.type = "submit";
                                    button.textContent = "Delete song";
                                    form.addEventListener("submit", (e) => {
                                        e.preventDefault();
                                        makeCall("POST", 'DeleteSong', form, (x) => {
                                            if (x.readyState == XMLHttpRequest.DONE) {
                                                switch (x.status) {
                                                    case 200:
                                                        this.showAllSongs();
                                                        break;
                                                    default:
                                                        this.showAllSongs();
                                                        alert("Invalid deletion");
                                                        break;

                                                }
                                            }
                                        });
                                    });
                                    form.appendChild(button);
                                    th.appendChild(form);
                                    tr.appendChild(th);
                                    songstable.appendChild(tr);
                                });
                            }
                            break;
                        default:
                            alert("Can't get the complete song list");
                            break;
                    }
                }
            });
            songstable.style.display = "table";
            div.style.display = "block";
        }
    }

    function SongPage(){
        let div = document.getElementById("songpage");
        let closeform = document.getElementById("closesongpage");
        let songtitle = document.getElementById("songtitle");
        let songartist = document.getElementById("songartist");
        let songalbum = document.getElementById("songalbum");
        let songgenre = document.getElementById("songgenre");
        let songyear = document.getElementById("songyear");
        let songalbumcover = document.getElementById("songalbumcover");
        let songaudio = document.getElementById("songaudio");
        let line = document.getElementById("line2");

        this.init = function(){
            this.close();
            closeform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.close();
            });
        }

        this.close = function(){
            div.style.display = "none";
            songtitle.textContent = "";
            songartist.textContent = "";
            songalbum.textContent = "";
            songgenre.textContent = "";
            songyear.textContent = "";
            songalbumcover.src = "";
            songaudio.src = "";
            closeform.style.display = "none";
            line.style.display = "none";
        }

        this.show = function(songid){
            makeCall("GET", 'GetSong?songId=' + songid, null, (x) => {
                if (x.readyState == XMLHttpRequest.DONE) {
                    switch (x.status) {
                        case 200:
                            let response = JSON.parse(x.responseText);
                            songtitle.textContent = response.title;
                            songartist.textContent = response.artist;
                            songalbum.textContent = response.album;
                            songgenre.textContent = response.genre;
                            songyear.textContent = response.year;
                            songalbumcover.src = ("data:image/png;base64," + response.encodedAlbumCover);
                            songaudio.src = ("data:audio/mpeg;base64," + response.encodedSong);
                            break;
                        default:
                            alert("Can't get song data");
                            break;
                    }
                }
            });

            closeform.style.display = "block";
            div.style.display = "block";
            line.style.display = "block";

        }

    }

    function PlaylistOrdinator(){
        let div = document.getElementById("playlistordinator");
        let ordinatortable = document.getElementById("playlistordinatortable");
        let closeplaylistordinatorform = document.getElementById("closeplaylistordinatorform");
        let submitplaylistordinatorform = document.getElementById("submitplaylistordinatorform");
        let songList;
        let idList;
        let draggedId;
        let droppedId;
        let playlistIdToReorder;

        this.init = function(){
            this.close();
            div.style.display = "none";
            ordinatortable.style.display = "none";
            closeplaylistordinatorform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.close();
            });
            submitplaylistordinatorform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit();
            });
        }

        this.close = function(){
            div.style.display = "none";
            ordinatortable.style.display = "none";
            ordinatortable.innerHTML = "";
            songList = null;
            idList = null;
        }

        this.show = function(playlistId){
            playlistIdToReorder = playlistId;
            showOnePlaylist.close();
            makeCall("GET", 'GetPlaylistSongs?playlistId=' + playlistId, null, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            div.style.display = "block";
                            ordinatortable.style.display = "table";
                            let response = JSON.parse(x.responseText);
                            if(response.length > 0){
                                songList = response;
                                idList = songList.map((song) => song.id);
                                ordinatortable.innerHTML = "";
                                this.rePrint();
                            }
                    }
                }
            });
        }

        this.rePrint = function(){
            ordinatortable.innerHTML = "";

            songList.forEach((song) => {
                let tr = document.createElement("tr");
                let th = document.createElement("th");
                let img = document.createElement("img");
                img.src = ("data:image/png;base64," + song.encodedAlbumCover);
                img.width = 100;

                img.draggable = true;
                img.addEventListener("dragstart", (e) => {
                    draggedId = song.id;
                });
                img.addEventListener("dragenter", (e) => {
                    //droppedId = parseInt(e.dataTransfer.getData("text"));
                    droppedId = song.id;
                });
                img.addEventListener("dragover", (e) => {
                    e.preventDefault();
                });
                img.addEventListener("dragleave", (e) => {
                    e.preventDefault();
                    droppedId = null;
                });
                img.addEventListener("drop", (e) => {
                    e.preventDefault();
                    if(droppedId != null){
                        let index1 = idList.indexOf(draggedId);
                        let index2 = idList.indexOf(droppedId);
                        //console.log(index1);
                        //console.log(index2);
                        let temp = idList[index1];
                        idList[index1] = idList[index2];
                        idList[index2] = temp;
                        let temp2 = songList[index1];
                        songList[index1] = songList[index2];
                        songList[index2] = temp2;

                        index1 = null;
                        index2 = null;
                        temp = null;
                        temp2 = null;

                        this.rePrint();
                    }
                });
                img.addEventListener("dragend", (e) => {
                    e.preventDefault();
                });

                th.appendChild(img);
                let br = document.createElement("br");
                th.appendChild(br);
                let newdiv = document.createElement("div");
                newdiv.textContent = song.title;
                th.appendChild(newdiv);
                tr.appendChild(th);
                ordinatortable.appendChild(tr);
            });

        }

        this.submit = function(){

            let formElement = document.createElement("form");

            let inputElement = document.createElement("input");
            inputElement.type = "hidden";
            inputElement.name = "songIds";
            inputElement.value = idList.join(",");
            formElement.appendChild(inputElement);

            makeCall("POST", 'ReorderPlaylistSongs?playlistId=' + playlistIdToReorder, formElement, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            this.close();
                            alert("Playlist reordered!");
                            break;
                        default:
                            alert("Can't reorder playlist");
                            break;
                    }
                }
            });
        }
    }

    function Logout(){
        let logoutbutton = document.getElementById("logoutbutton");
        let deleteacccountbutton = document.getElementById("deleteaccountbutton");

        this.init = function(){
            logoutbutton.addEventListener("submit", (e) => {
                    e.preventDefault();
                this.logout();
            });
            deleteacccountbutton.addEventListener("submit", (e) => {
               e,preventDefault();
                this.deleteAccount();
            });
        }

        this.logout = function(){
            makeCall("POST", 'Logout', null, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            window.location.href = "index.html";
                            localStorage.removeItem("username");
                            localStorage.removeItem("userId");
                            break;
                        default:
                            alert("Can't logout");
                            break;
                    }
                }
            });
        }
        this.deleteAccount = function(){
            makeCall("POST", 'DeleteAccount', null, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            window.location.href = "index.html";
                            localStorage.removeItem("username");
                            localStorage.removeItem("userId");
                            break;
                        default:
                            alert("Can't delete account");
                            break;
                    }
                }
            });
        }
    }

    function PageOrchestrator(){
        this.start = function(){
            document.getElementById("hometext").textContent = ("Playlist Home - " + localStorage.getItem("username"));
            playlistList.init();
            showOnePlaylist.init();
            createPlaylist.init();
            addSongToPlaylist.init();
            playlistPageTitle.init();
            theActualPlaylist.init();
            deletePlaylist.init();
            uploadSong.init();
            allSongs.init();
            songPage.init();
            playlistOrdinator.init();
            logout.init();
        }
    }
}